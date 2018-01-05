package rmi;

public class Manager {
    private static ThreadLocal<Context> context = new ThreadLocal<>();

    public static Context getContext() {
        return context.get();
    }

    public static void setContext(Context ctx) {
        Manager.context.set(ctx);
    }

}
