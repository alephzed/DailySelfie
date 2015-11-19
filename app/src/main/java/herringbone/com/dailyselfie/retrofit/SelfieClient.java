package herringbone.com.dailyselfie.retrofit;

import retrofit.client.Response;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

public interface SelfieClient {
    @Multipart
    @POST("/photo/process")
    Response process(
            @Part("instructions") String owner,
            @Part("file") TypedFile file
    );
}
