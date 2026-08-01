package com.nttbp.jfw2.api.encrypt;

import K7.a;
import com.nttbp.jfw2.lib.encrypt.AESStringEncryptor;
import kotlin.Metadata;
import kotlin.collections.ArraysKt___ArraysKt;
import kotlin.jvm.internal.Intrinsics;
import l9.C2004c;
import m9.D;
import m9.u;
import m9.z;
import x7.C2679a;

/* compiled from: EncryptableApiRequestBodyJsonAdapter.kt */
@Metadata(d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0012\n\u0002\b\b\u0018\u0000*\u0004\b\u0000\u0010\u00012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00028\u00000\u00030\u0002B%\u0012\u0006\u0010\u0005\u001a\u00020\u0004\u0012\u0006\u0010\u0007\u001a\u00020\u0006\u0012\f\u0010\b\u001a\b\u0012\u0004\u0012\u00028\u00000\u0002¢\u0006\u0004\b\t\u0010\nR\u0014\u0010\u0005\u001a\u00020\u00048\u0002X\u0082\u0004¢\u0006\u0006\n\u0004\b\u0005\u0010\u000bR\u0014\u0010\u0007\u001a\u00020\u00068\u0002X\u0082\u0004¢\u0006\u0006\n\u0004\b\u0007\u0010\fR\u001a\u0010\b\u001a\b\u0012\u0004\u0012\u00028\u00000\u00028\u0002X\u0082\u0004¢\u0006\u0006\n\u0004\b\b\u0010\r¨\u0006\u000e"}, d2 = {"Lcom/nttbp/jfw2/api/encrypt/EncryptableApiRequestBodyJsonAdapter;", "DataType", "Lm9/u;", "Lx7/a;", "", "apiKey", "", "encryptionCommonKey", "dataJsonAdapter", "<init>", "(Ljava/lang/String;[BLm9/u;)V", "Ljava/lang/String;", "[B", "Lm9/u;", "app_productionRelease"}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final class EncryptableApiRequestBodyJsonAdapter<DataType> extends u<C2679a<DataType>> {
    private final String apiKey;
    private final u<DataType> dataJsonAdapter;
    private final byte[] encryptionCommonKey;

    public EncryptableApiRequestBodyJsonAdapter(String apiKey, byte[] encryptionCommonKey, u<DataType> dataJsonAdapter) {
        Intrinsics.f(apiKey, "apiKey");
        Intrinsics.f(encryptionCommonKey, "encryptionCommonKey");
        Intrinsics.f(dataJsonAdapter, "dataJsonAdapter");
        this.apiKey = apiKey;
        this.encryptionCommonKey = encryptionCommonKey;
        this.dataJsonAdapter = dataJsonAdapter;
    }

    @Override // m9.u
    public final Object b(z reader) {
        Intrinsics.f(reader, "reader");
        throw new UnsupportedOperationException();
    }

    @Override // m9.u
    public final void g(D writer, Object obj) {
        C2679a c2679a = (C2679a) obj;
        Intrinsics.f(writer, "writer");
        if (c2679a == null) {
            return;
        }
        String f10 = this.dataJsonAdapter.f(c2679a.f35873a);
        a a10 = new AESStringEncryptor(this.encryptionCommonKey).a(f10);
        String concat = "dataJson          : ".concat(f10);
        String str = a10.f4604a;
        String concat2 = "dataJsonEncrypted : ".concat(str);
        StringBuilder sb = new StringBuilder("iv                : ");
        String str2 = a10.f4605b;
        sb.append(str2);
        C2004c.a(ArraysKt___ArraysKt.B(new String[]{concat, concat2, sb.toString()}, "\n", null, null, null, 62), new Object[0]);
        D d10 = writer.d();
        d10.y("data");
        d10.O(str);
        d10.y("iv");
        d10.O(str2);
        d10.y("apikey");
        d10.O(this.apiKey);
        d10.o();
    }
}
