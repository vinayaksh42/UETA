public class DriverStub {

    public static String source0() {
        return "tainted-string";
    }

    public static int source1() {
        return 42;
    }

    public static void run() {
        new org.apache.httpcore.HttpHost(source0(), source1());
    }
}
