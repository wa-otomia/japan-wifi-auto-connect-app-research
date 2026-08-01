package x7;

import com.nttbp.jfw2.api.encrypt.EncryptableApiRequestBodyJsonAdapter;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;
import kotlin.collections.ArraysKt___ArraysKt;
import kotlin.jvm.internal.Intrinsics;
import m9.G;
import m9.K;
import m9.u;

/* compiled from: EncryptableApiRequestBodyJsonAdapterFactory.kt */
/* loaded from: classes.dex */
public final class b implements u.a {

    /* renamed from: a, reason: collision with root package name */
    public final String f35874a;

    /* renamed from: b, reason: collision with root package name */
    public final byte[] f35875b;

    public b(String apiKey, byte[] encryptionCommonKey) {
        Intrinsics.f(apiKey, "apiKey");
        Intrinsics.f(encryptionCommonKey, "encryptionCommonKey");
        this.f35874a = apiKey;
        this.f35875b = encryptionCommonKey;
    }

    @Override // m9.u.a
    public final u<?> a(Type type, Set<? extends Annotation> annotations, G moshi) {
        Intrinsics.f(type, "type");
        Intrinsics.f(annotations, "annotations");
        Intrinsics.f(moshi, "moshi");
        if (!Intrinsics.a(K.c(type), C2679a.class) || !(type instanceof ParameterizedType)) {
            return null;
        }
        Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
        Intrinsics.e(actualTypeArguments, "getActualTypeArguments(...)");
        u b10 = moshi.b((Type) ArraysKt___ArraysKt.s(actualTypeArguments));
        Intrinsics.e(b10, "adapter(...)");
        return new EncryptableApiRequestBodyJsonAdapter(this.f35874a, this.f35875b, b10);
    }
}
