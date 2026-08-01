package v7;

import L9.t;
import com.nttbp.jfw2.api.connection.ConnectionStartRequestParams;
import ec.H;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;
import x7.C2679a;

/* compiled from: CloudAPConnectionApi.kt */
/* renamed from: v7.a, reason: case insensitive filesystem */
/* loaded from: classes.dex */
public interface InterfaceC2578a {
    @Headers({"Content-Type: application/json"})
    @POST
    t<Response<H>> a(@Url String str, @Body C2679a<ConnectionStartRequestParams> c2679a);
}
