package E7;

import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;

/* compiled from: AppModule_EncryptionCommonKeyFactory.java */
/* renamed from: E7.z, reason: case insensitive filesystem */
/* loaded from: classes.dex */
public final class C0630z implements F9.b<byte[]> {

    /* renamed from: a, reason: collision with root package name */
    public final C0556a f2470a;

    public C0630z(C0556a c0556a) {
        this.f2470a = c0556a;
    }

    @Override // va.InterfaceC2586a
    public final Object get() {
        this.f2470a.getClass();
        byte[] bytes = "my$?[kq&)a+4j6l$".getBytes(Charsets.f31600b);
        Intrinsics.e(bytes, "getBytes(...)");
        return bytes;
    }
}
