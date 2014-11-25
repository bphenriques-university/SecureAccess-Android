package sirs.ist.pt.secureaccess.security;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestClas {

    public static void main(String[] args) throws Exception {

        final Runnable stuffToDo = new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println("sleeping...!");
                    Thread.sleep(10000);
                    System.out.println("Woke up!");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future future = executor.submit(stuffToDo);
        executor.shutdown(); // This does not cancel the already-scheduled task.
        try {
            future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            /* Handle the interruption. Or ignore it. */
        } catch (ExecutionException ee) {
            /* Handle the error. Or ignore it. */
        } catch (TimeoutException te) {
            /* Handle the timeout. Or ignore it. */
        }

        if (!executor.isTerminated()) {
            System.out.println("Failed to connect!");
            executor.shutdownNow(); // If you want to stop the code that hasn't finished.
        }
        else{
            System.out.println("Hurray!");
        }


    }


}