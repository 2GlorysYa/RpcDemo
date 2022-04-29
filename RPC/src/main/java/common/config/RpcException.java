package common.config;

public class RpcException extends Exception{

    RpcError e;
    String msg;

    public RpcException(RpcError e) {
        this.e = e;
    }

    public RpcException(RpcError e, String msg) {
        this.e = e;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
