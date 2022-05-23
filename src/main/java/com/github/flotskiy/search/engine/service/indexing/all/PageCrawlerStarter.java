package com.github.flotskiy.search.engine.service.indexing.all;

import com.github.flotskiy.search.engine.dataholders.CollectionsHolder;
import com.github.flotskiy.search.engine.model.Site;
import com.github.flotskiy.search.engine.model.Status;
import com.github.flotskiy.search.engine.util.StringHelper;
import com.github.flotskiy.search.engine.util.YmlConfigGetter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLHandshakeException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateExpiredException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class PageCrawlerStarter {
    private static final String INTERRUPTED_BY_USER_MESSAGE = "Indexing stopped by user";
    private static final String CERTIFICATE_ERROR = "Site's certificate validity check failed";
    private static final String CONNECTION_ERROR = "Connection timed out / failed";
    private static final String UNKNOWN_ERROR = "Unknown error";

    private final AtomicBoolean isStopped = new AtomicBoolean(false);
    private final AtomicBoolean isStarted = new AtomicBoolean(false);
    private final Map<String, String> SOURCES_MAP = YmlConfigGetter.getSites();
    private ForkJoinPool forkJoinPool;

    private final CollectionsHolder collectionsHolder;
    private final RepoFiller repoFiller;
    private final CollFiller collFiller;

    @Autowired
    public PageCrawlerStarter(CollectionsHolder collectionsHolder, RepoFiller repoFiller, CollFiller collFiller) {
        this.collectionsHolder = collectionsHolder;
        this.repoFiller = repoFiller;
        this.collFiller = collFiller;
    }

    public void startCrawling() {
        isStopped.set(false);
        isStarted.set(true);
        if (SOURCES_MAP.size() < 1) {
            System.out.println("No sites specified!");
            return;
        }
        long start = System.currentTimeMillis();
        forkJoinPool = new ForkJoinPool();
        collFiller.setSelectorsAndWeigh();
        collFiller.fillInSiteList(SOURCES_MAP);
        repoFiller.fillInFields();
        for (Site site : new ArrayList<>(collectionsHolder.getSiteList())) {
            boolean isSuccessfully = processSite(site);
            if (!isSuccessfully) {
                break;
            }
            if (isStopped.get()) {
                fixErrorAndClearCollections(site, INTERRUPTED_BY_USER_MESSAGE);
                break;
            }
        }
        isStopped.set(true);
        System.out.println("Duration of processing: " + (System.currentTimeMillis() - start) / 1000 + " s");
        isStarted.set(false);
    }

    private boolean processSite(Site site) {
        try {
            String homePage = makeActionsBeforeForkJoinPoolStarted(site);
            PageCrawler pageCrawler = new PageCrawler(homePage, site, collFiller);
            forkJoinPool.invoke(pageCrawler);
            completeActionsAfterForkJoinPoolFinished(site);
        } catch (ConnectException ce) {
            System.out.println("ConnectionException in PageCrawlerStarter");
            fixErrorAndClearCollections(site, CONNECTION_ERROR);
            return false;
        } catch (CancellationException ce) {
            System.out.println("CancellationException in PageCrawlerStarter");
            fixErrorAndClearCollections(site, INTERRUPTED_BY_USER_MESSAGE);
            return false;
        } catch (CertificateExpiredException | SSLHandshakeException | CertPathValidatorException certEx) {
            System.out.println("CertificateException in PageCrawlerStarter");
            fixErrorAndClearCollections(site, CERTIFICATE_ERROR);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception in PageCrawlerStarter");
            fixErrorAndClearCollections(site, UNKNOWN_ERROR);
            return false;
        }
        return true;
    }

    public void stopIndexing() {
        isStopped.set(true);
        forkJoinPool.shutdownNow();
    }

    public void fixErrorAndClearCollections(Site site, String message) {
        repoFiller.setFailedStatus(site, message);
        collFiller.clearCollections();
        collFiller.clearSelectorsAndWeightCollection();
    }

    private String makeActionsBeforeForkJoinPoolStarted(Site site) throws MalformedURLException {
        repoFiller.setSiteStatus(site, Status.INDEXING);
        repoFiller.deletePreviouslyIndexedSiteByName(site.getName(), site.getId());
        repoFiller.saveSite(site);
        return StringHelper.getHomePage(site.getUrl());
    }

    private void completeActionsAfterForkJoinPoolFinished(Site site){
        repoFiller.fillInPages();
        repoFiller.fillInLemmas();
        repoFiller.fillInSearchIndex();
        if (isStopped.get()) {
            return;
        }
        repoFiller.setSiteStatus(site, Status.INDEXED);
        collFiller.clearCollections();
    }

    public boolean isStopped() {
        return isStopped.get();
    }

    public boolean isStarted() {
        return isStarted.get();
    }
}
