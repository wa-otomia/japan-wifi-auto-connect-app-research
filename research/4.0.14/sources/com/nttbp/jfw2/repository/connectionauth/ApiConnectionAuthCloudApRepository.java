package com.nttbp.jfw2.repository.connectionauth;

import B3.i;
import B7.b;
import C8.A;
import C8.C;
import C8.C0455e;
import C8.H;
import C8.J;
import C8.S0;
import C8.T0;
import E8.R0;
import F8.U;
import F8.X;
import L7.g;
import L9.t;
import M7.d;
import Sb.q;
import U8.h;
import Ub.C1011f;
import Vb.c;
import android.content.Context;
import ba.C1315i;
import ba.l;
import ba.o;
import com.applovin.sdk.AppLovinEventParameters;
import com.applovin.sdk.AppLovinEventTypes;
import com.google.android.gms.common.internal.ImagesContract;
import com.ironsource.mediationsdk.utils.IronSourceConstants;
import com.nttbp.jfw2.api.connection.ConnectionStartRequestParams;
import com.nttbp.jfw2.api.error.APIErrorException;
import com.nttbp.jfw2.api.error.APIResponseType;
import com.nttbp.jfw2.model.DLinkEndPointUrlParam;
import com.nttbp.jfw2.model.UserSettings;
import com.nttbp.jfw2.model.user.UserLanguageType;
import com.nttbp.jfw2.repository.authhistory.CloudAPEndpointJson;
import com.nttbp.jfw2.repository.authhistory.CloudAPErrorJson;
import com.nttbp.jfw2.repository.authhistory.CloudAPSessionJson;
import com.nttbp.jfw2.repository.connectionauth.ApiConnectionAuthCloudApRepository;
import com.nttbp.jw2.R;
import d8.C1572a;
import ec.B;
import ec.r;
import ec.z;
import f8.C1680m;
import g8.C1722j;
import i8.C1817l;
import j8.C1898b;
import j8.C1899c;
import j8.C1900d;
import j8.C1903g;
import j8.C1906j;
import j8.InterfaceC1912p;
import java.math.BigInteger;
import java.security.SecureRandom;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref;
import kotlin.jvm.internal.SourceDebugExtension;
import m9.G;
import retrofit2.Response;
import retrofit2.Retrofit;
import v7.InterfaceC2578a;
import x7.C2679a;

/* compiled from: ApiConnectionAuthCloudApRepository.kt */
@SourceDebugExtension
/* loaded from: classes.dex */
public final class ApiConnectionAuthCloudApRepository implements InterfaceC1912p {

    /* renamed from: a, reason: collision with root package name */
    public final z f24738a;

    /* renamed from: b, reason: collision with root package name */
    public final z f24739b;

    /* renamed from: c, reason: collision with root package name */
    public final d f24740c;

    /* renamed from: d, reason: collision with root package name */
    public final UserSettings f24741d = UserSettings.g;

    /* renamed from: e, reason: collision with root package name */
    public final String f24742e;

    /* renamed from: f, reason: collision with root package name */
    public final InterfaceC2578a f24743f;

    /* compiled from: ApiConnectionAuthCloudApRepository.kt */
    @Metadata(d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003R\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0086T¢\u0006\u0002\n\u0000¨\u0006\u0007"}, d2 = {"Lcom/nttbp/jfw2/repository/connectionauth/ApiConnectionAuthCloudApRepository$Companion;", "", "<init>", "()V", "DLINK_URL_PATTERN", "", "DLINK_CAPTIVE_PORTAL_URL", "app_productionRelease"}, k = 1, mv = {2, 0, 0}, xi = 48)
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }
    }

    static {
        new Companion(null);
    }

    public ApiConnectionAuthCloudApRepository(Context context, Retrofit retrofit, z zVar, z zVar2, d dVar) {
        this.f24738a = zVar;
        this.f24739b = zVar2;
        this.f24740c = dVar;
        String string = context.getString(R.string.internet_connection_check_url);
        Intrinsics.e(string, "getString(...)");
        this.f24742e = string;
        Object create = retrofit.create(InterfaceC2578a.class);
        Intrinsics.e(create, "create(...)");
        this.f24743f = (InterfaceC2578a) create;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static APIErrorException k(int i2, String str) {
        String statusCode;
        CloudAPErrorJson.Response.Body body;
        CloudAPErrorJson cloudAPErrorJson = (CloudAPErrorJson) new G(new G.a()).a(CloudAPErrorJson.class).d().a(str);
        if (cloudAPErrorJson == null) {
            throw new APIErrorException(String.valueOf(APIResponseType.PARSE_ERROR.getErrorCode()), "json parse error", null, null, null, 28);
        }
        CloudAPErrorJson.Response response = cloudAPErrorJson.getResponse();
        if (response == null || (body = response.getBody()) == null || (statusCode = body.getStatus()) == null) {
            CloudAPErrorJson.Response response2 = cloudAPErrorJson.getResponse();
            statusCode = response2 != null ? response2.getStatusCode() : null;
            if (statusCode == null) {
                statusCode = APIResponseType.INSTANCE.convertCloudCode(cloudAPErrorJson.getCode(), i2);
            }
        }
        return new APIErrorException(statusCode, cloudAPErrorJson.getMessage(), null, null, null, 28);
    }

    @Override // j8.InterfaceC1912p
    public final t<APIResponseType> a(final String url, String state) {
        Intrinsics.f(url, "url");
        Intrinsics.f(state, "state");
        if (state.equals("") || !q.l(url, AppLovinEventTypes.USER_LOGGED_IN, false)) {
            return t.d(APIResponseType.SUCCESS);
        }
        B.a aVar = new B.a();
        aVar.c();
        aVar.g(url);
        return new o(new l(C1011f.b(this.f24738a.a(aVar.b())), new g(new C(2), 1)), new R9.o(this) { // from class: j8.i

            /* renamed from: b, reason: collision with root package name */
            public final /* synthetic */ ApiConnectionAuthCloudApRepository f28021b;

            {
                this.f28021b = this;
            }

            @Override // R9.o
            public final Object apply(Object obj) {
                Throwable it = (Throwable) obj;
                Intrinsics.f(it, "it");
                throw APIErrorException.g.make(it, url, this.f28021b.f24740c);
            }
        }, null);
    }

    /* JADX WARN: Type inference failed for: r7v10, types: [T, java.lang.Object, java.lang.String] */
    @Override // j8.InterfaceC1912p
    public final o b(final CloudAPSessionJson.Redirect redirect, DLinkEndPointUrlParam dLinkEndPointUrlParam) {
        B b10;
        String str;
        String continue_url;
        String success_url;
        String password;
        String url;
        String cmd;
        String user;
        String username;
        Ref.ObjectRef objectRef = new Ref.ObjectRef();
        objectRef.f28643a = "";
        if (Intrinsics.a(redirect.getMethod(), "POST")) {
            r.a aVar = new r.a(0);
            CloudAPSessionJson.Redirect.Params params = redirect.getParams();
            if (params != null && (username = params.getUsername()) != null) {
                aVar.b(AppLovinEventParameters.USER_ACCOUNT_IDENTIFIER, username);
            }
            CloudAPSessionJson.Redirect.Params params2 = redirect.getParams();
            if (params2 != null && (user = params2.getUser()) != null) {
                aVar.b("user", user);
            }
            CloudAPSessionJson.Redirect.Params params3 = redirect.getParams();
            if (params3 != null && (cmd = params3.getCmd()) != null) {
                aVar.b("cmd", cmd);
            }
            CloudAPSessionJson.Redirect.Params params4 = redirect.getParams();
            if (params4 != null && (url = params4.getUrl()) != null) {
                aVar.b(ImagesContract.URL, url);
            }
            CloudAPSessionJson.Redirect.Params params5 = redirect.getParams();
            if (params5 != null && (password = params5.getPassword()) != null) {
                aVar.b("password", password);
            }
            CloudAPSessionJson.Redirect.Params params6 = redirect.getParams();
            if (params6 != null && (success_url = params6.getSuccess_url()) != null) {
                aVar.b("success_url", success_url);
            }
            CloudAPSessionJson.Redirect.Params params7 = redirect.getParams();
            if (params7 != null && (continue_url = params7.getContinue_url()) != null) {
                aVar.b("continue_url", continue_url);
            }
            if (Intrinsics.a(dLinkEndPointUrlParam, DLinkEndPointUrlParam.f24426k.getEMPTY())) {
                str = redirect.getUrl();
            } else {
                SecureRandom secureRandom = new SecureRandom();
                BigInteger bigInteger = BigInteger.TEN;
                ?? bigInteger2 = new BigInteger(bigInteger.pow(22).bitLength(), secureRandom).add(bigInteger.pow(23).subtract(BigInteger.ONE)).toString();
                Intrinsics.e(bigInteger2, "toString(...)");
                objectRef.f28643a = bigInteger2;
                str = redirect.getUrl() + "?state=" + objectRef.f28643a;
            }
            r c10 = aVar.c();
            B.a aVar2 = new B.a();
            aVar2.f(c10);
            aVar2.g(str);
            b10 = aVar2.b();
        } else {
            B.a aVar3 = new B.a();
            aVar3.c();
            aVar3.g(redirect.getUrl());
            b10 = aVar3.b();
        }
        return new o(new l(C1011f.b(this.f24739b.a(b10)), new H(new C1906j(objectRef, this), 3)), new R9.o() { // from class: j8.k
            @Override // R9.o
            public final Object apply(Object obj) {
                Throwable it = (Throwable) obj;
                Intrinsics.f(it, "it");
                throw APIErrorException.g.make(it, CloudAPSessionJson.Redirect.this.getUrl(), this.f24740c);
            }
        }, null);
    }

    @Override // j8.InterfaceC1912p
    public final t<Pair<APIResponseType, String>> c(String url) {
        Intrinsics.f(url, "url");
        if (!q.l(url, "complete", false)) {
            return t.d(new Pair(APIResponseType.SUCCESS, ""));
        }
        B.a aVar = new B.a();
        aVar.c();
        aVar.g(url);
        return new o(new l(C1011f.b(this.f24738a.a(aVar.b())), new C1572a(new R0(1), 5)), new C1817l(this, url), null);
    }

    @Override // j8.InterfaceC1912p
    public final t<Pair<APIResponseType, String>> d(String dispatcherUrl, String token, String state, String completeUrl) {
        Intrinsics.f(dispatcherUrl, "dispatcherUrl");
        Intrinsics.f(token, "token");
        Intrinsics.f(state, "state");
        Intrinsics.f(completeUrl, "completeUrl");
        return !state.equals("") ? l(state) : q.l(completeUrl, "complete", false) ? t.d(new Pair(APIResponseType.SUCCESS, completeUrl)) : t.d(new Pair(APIResponseType.SUCCESS, dispatcherUrl));
    }

    @Override // j8.InterfaceC1912p
    public final o e(CloudAPEndpointJson cloudAPEndpointJson, String str, long j10, String uuid, String str2, String str3, String str4) {
        Intrinsics.f(cloudAPEndpointJson, "cloudAPEndpointJson");
        Intrinsics.f(uuid, "uuid");
        t<Response<ec.H>> a10 = this.f24743f.a(cloudAPEndpointJson.getAuthentication(), new C2679a<>(new ConnectionStartRequestParams(str, null, null, String.valueOf(j10), uuid, str2, str3, str4, 6, null)));
        S0 s02 = new S0(new c(1, cloudAPEndpointJson, this), 4);
        a10.getClass();
        return new o(new l(a10, s02), new B5.c(cloudAPEndpointJson, this), null);
    }

    @Override // j8.InterfaceC1912p
    public final C1315i f(final String url, String state) {
        t d10;
        Intrinsics.f(url, "url");
        Intrinsics.f(state, "state");
        if (url.length() > 0) {
            B.a aVar = new B.a();
            aVar.c();
            aVar.g(url);
            d10 = new o(new l(C1011f.b(this.f24738a.a(aVar.b())), new C1900d(new X(1), 0)), new R9.o(this) { // from class: j8.e

                /* renamed from: b, reason: collision with root package name */
                public final /* synthetic */ ApiConnectionAuthCloudApRepository f28013b;

                {
                    this.f28013b = this;
                }

                @Override // R9.o
                public final Object apply(Object obj) {
                    Throwable it = (Throwable) obj;
                    Intrinsics.f(it, "it");
                    throw APIErrorException.g.make(it, url, this.f28013b.f24740c);
                }
            }, null);
        } else {
            d10 = t.d(APIResponseType.SUCCESS);
        }
        return new C1315i(d10, new C1680m(new C1899c(this, state), 1));
    }

    @Override // j8.InterfaceC1912p
    public final o g(String url, String token, String authentication_token) {
        Intrinsics.f(url, "url");
        Intrinsics.f(token, "token");
        Intrinsics.f(authentication_token, "authentication_token");
        r.a aVar = new r.a(0);
        aVar.b("state", token);
        aVar.b("code", authentication_token);
        r c10 = aVar.c();
        B.a aVar2 = new B.a();
        aVar2.a("ContentType", "application/json");
        aVar2.g(url);
        aVar2.f(c10);
        return new o(new l(C1011f.b(this.f24738a.a(aVar2.b())), new U(new h(this, 1), 4)), new C1722j(url, this), null);
    }

    @Override // j8.InterfaceC1912p
    public final o h(final String url, String token) {
        Intrinsics.f(url, "url");
        Intrinsics.f(token, "token");
        B.a aVar = new B.a();
        aVar.a("accept", "application/json");
        aVar.g(url + "?token=" + token + "&mode=post&lang=" + UserLanguageType.f24575f.getLocaleLanguageCodes());
        aVar.c();
        return new o(new l(C1011f.b(this.f24738a.a(aVar.b())), new i(new J(3), 9)), new R9.o(this) { // from class: j8.a

            /* renamed from: b, reason: collision with root package name */
            public final /* synthetic */ ApiConnectionAuthCloudApRepository f28005b;

            {
                this.f28005b = this;
            }

            @Override // R9.o
            public final Object apply(Object obj) {
                Throwable it = (Throwable) obj;
                Intrinsics.f(it, "it");
                throw APIErrorException.g.make(it, url, this.f28005b.f24740c);
            }
        }, null);
    }

    @Override // j8.InterfaceC1912p
    public final o i(final String url) {
        Intrinsics.f(url, "url");
        B.a aVar = new B.a();
        aVar.a("accept", "application/json");
        aVar.g(url);
        aVar.c();
        return new o(new l(C1011f.b(this.f24738a.a(aVar.b())), new T0(new b(3), 2)), new R9.o(this) { // from class: j8.f

            /* renamed from: b, reason: collision with root package name */
            public final /* synthetic */ ApiConnectionAuthCloudApRepository f28015b;

            {
                this.f28015b = this;
            }

            @Override // R9.o
            public final Object apply(Object obj) {
                Throwable it = (Throwable) obj;
                Intrinsics.f(it, "it");
                throw APIErrorException.g.make(it, url, this.f28015b.f24740c);
            }
        }, null);
    }

    @Override // j8.InterfaceC1912p
    public final o j(final String str, DLinkEndPointUrlParam dLinkEndPointUrlParam) {
        if (str == null && (str = this.f24741d.d()) == null) {
            str = this.f24742e;
        }
        B.a aVar = new B.a();
        aVar.g(str);
        aVar.c();
        return new o(new C1315i(C1011f.b(this.f24739b.a(aVar.b())), new A(new C1903g(dLinkEndPointUrlParam, this), 3)), new R9.o(this) { // from class: j8.h

            /* renamed from: b, reason: collision with root package name */
            public final /* synthetic */ ApiConnectionAuthCloudApRepository f28019b;

            {
                this.f28019b = this;
            }

            @Override // R9.o
            public final Object apply(Object obj) {
                Throwable it = (Throwable) obj;
                Intrinsics.f(it, "it");
                throw APIErrorException.g.make(it, str, this.f28019b.f24740c);
            }
        }, null);
    }

    public final C1315i l(String str) {
        r.a aVar = new r.a(0);
        aVar.a("ccp_act", IronSourceConstants.EVENTS_STATUS);
        aVar.a("tid", str);
        r c10 = aVar.c();
        B.a aVar2 = new B.a();
        aVar2.g("http://jw2.cdn.wifi-cloud.jp/captive_portal.ccp");
        aVar2.f(c10);
        return new C1315i(C1011f.b(this.f24738a.a(aVar2.b())), new C0455e(new C1898b(this, str), 4));
    }
}
