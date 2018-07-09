/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import scala.collection.Seq;
import scala.concurrent.Future;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static scala.collection.JavaConverters.asJavaIterable;
import static scala.compat.java8.FutureConverters.globalExecutionContext;
import static scala.compat.java8.FutureConverters.toJava;

public class PackageTest {

    @Test
    public void testDiscoverFromConfig() throws InterruptedException, ExecutionException {
        Future<Seq<String>> fut = package$.MODULE$.discoverFromConfig(globalExecutionContext());
        CompletionStage<Seq<String>> cs = toJava(fut);
        CompletableFuture<Seq<String>> cp = (CompletableFuture<Seq<String>>) cs;
        Seq<String> result = cp.get();
        assertNotEquals(result.size(), 0);
        // TODO rewrite to use regex
        assertThat(asJavaIterable(result), everyItem(startsWith("172.31.")));
    }
}
