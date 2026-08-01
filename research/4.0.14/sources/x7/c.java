package x7;

import E7.C0565d;
import E7.C0630z;

/* compiled from: EncryptableApiRequestBodyJsonAdapterFactory_Factory.java */
/* loaded from: classes.dex */
public final class c implements F9.b<b> {

    /* renamed from: a, reason: collision with root package name */
    public final C0565d f35876a;

    /* renamed from: b, reason: collision with root package name */
    public final C0630z f35877b;

    public c(C0565d c0565d, C0630z c0630z) {
        this.f35876a = c0565d;
        this.f35877b = c0630z;
    }

    @Override // va.InterfaceC2586a
    public final Object get() {
        this.f35876a.get();
        return new b("nttbp", (byte[]) this.f35877b.get());
    }
}
