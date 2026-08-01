package x7;

import kotlin.jvm.internal.Intrinsics;

/* compiled from: EncryptableApiRequestBody.kt */
/* renamed from: x7.a, reason: case insensitive filesystem */
/* loaded from: classes.dex */
public final class C2679a<DataType> {

    /* renamed from: a, reason: collision with root package name */
    public final DataType f35873a;

    public C2679a(DataType datatype) {
        this.f35873a = datatype;
    }

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof C2679a) && Intrinsics.a(this.f35873a, ((C2679a) obj).f35873a);
    }

    public final int hashCode() {
        DataType datatype = this.f35873a;
        if (datatype == null) {
            return 0;
        }
        return datatype.hashCode();
    }

    public final String toString() {
        return "EncryptableApiRequestBody(data=" + this.f35873a + ")";
    }
}
