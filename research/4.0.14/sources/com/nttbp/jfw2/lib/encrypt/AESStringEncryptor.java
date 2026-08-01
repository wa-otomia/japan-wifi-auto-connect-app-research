package com.nttbp.jfw2.lib.encrypt;

import Sa.j;
import Sa.r;
import android.util.Base64;
import java.util.ArrayList;
import java.util.Iterator;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import kotlin.Metadata;
import kotlin.collections.IntIterator;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.random.Random;
import kotlin.ranges.IntProgressionIterator;
import kotlin.ranges.IntRange;
import kotlin.ranges.a;
import kotlin.text.Charsets;

/* compiled from: AESStringEncryptor.kt */
@SourceDebugExtension
/* loaded from: classes.dex */
public final class AESStringEncryptor {

    /* renamed from: a, reason: collision with root package name */
    public final byte[] f24421a;

    /* renamed from: b, reason: collision with root package name */
    public final String f24422b;

    /* renamed from: c, reason: collision with root package name */
    public final Cipher f24423c;

    /* compiled from: AESStringEncryptor.kt */
    @Metadata(d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082T¢\u0006\u0002\n\u0000¨\u0006\b"}, d2 = {"Lcom/nttbp/jfw2/lib/encrypt/AESStringEncryptor$Companion;", "", "<init>", "()V", "IV_LENGTH", "", "IV_SOURCE", "", "app_productionRelease"}, k = 1, mv = {2, 0, 0}, xi = 48)
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

    public AESStringEncryptor(byte[] commonKey) {
        Intrinsics.f(commonKey, "commonKey");
        this.f24421a = commonKey;
        IntRange d10 = a.d(0, 16);
        ArrayList arrayList = new ArrayList(j.l(d10));
        Iterator<Integer> it = d10.iterator();
        while (((IntProgressionIterator) it).f28682c) {
            ((IntIterator) it).a();
            Random.f28663a.getClass();
            arrayList.add(Integer.valueOf(Random.f28664b.c()));
        }
        ArrayList arrayList2 = new ArrayList(j.l(arrayList));
        Iterator it2 = arrayList.iterator();
        while (it2.hasNext()) {
            arrayList2.add(Character.valueOf("abcdefghijklmnopqrstuvwxyz".charAt(((Number) it2.next()).intValue())));
        }
        String G4 = r.G(arrayList2, "", null, null, null, 62);
        this.f24422b = G4;
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(this.f24421a, "AES");
        byte[] bytes = G4.getBytes(Charsets.f31600b);
        Intrinsics.e(bytes, "getBytes(...)");
        cipher.init(1, secretKeySpec, new IvParameterSpec(bytes));
        this.f24423c = cipher;
    }

    public final K7.a a(String str) {
        byte[] bytes = str.getBytes(Charsets.f31600b);
        Intrinsics.e(bytes, "getBytes(...)");
        String encodeToString = Base64.encodeToString(this.f24423c.doFinal(bytes), 2);
        Intrinsics.e(encodeToString, "encodeToString(...)");
        return new K7.a(encodeToString, this.f24422b);
    }
}
