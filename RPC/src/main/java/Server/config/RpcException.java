package Server.config;

public class RpcException extends Exception{

    RpcError e;

    public RpcException(RpcError e) {
        this.e = e;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
