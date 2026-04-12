/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: D:\AndroidApp\MaterialFile\tools\android-sdk\build-tools\35.0.0\aidl.exe -pD:\AndroidApp\MaterialFile\tools\android-sdk\platforms\android-36\framework.aidl -oD:\AndroidApp\MaterialFile\materialfile_deps\android\libsu-service\build\generated\aidl_source_output_dir\release\out -ID:\AndroidApp\MaterialFile\materialfile_deps\android\libsu-service\src\aidl -ID:\AndroidApp\MaterialFile\materialfile_deps\android\libsu-service\src\release\aidl -dC:\Users\npznn\AppData\Local\Temp\aidl17483889061574742313.d D:\AndroidApp\MaterialFile\materialfile_deps\android\libsu-service\src\aidl\com\topjohnwu\superuser\internal\IRootServiceManager.aidl
 */
package com.topjohnwu.superuser.internal;
// Declare any non-default types here with import statements
public interface IRootServiceManager extends android.os.IInterface
{
  /** Default implementation for IRootServiceManager. */
  public static class Default implements com.topjohnwu.superuser.internal.IRootServiceManager
  {
    @Override public void broadcast(int uid) throws android.os.RemoteException
    {
    }
    @Override public void stop(android.content.ComponentName name, int uid) throws android.os.RemoteException
    {
    }
    @Override public void connect(android.os.IBinder binder) throws android.os.RemoteException
    {
    }
    @Override public android.os.IBinder bind(android.content.Intent intent) throws android.os.RemoteException
    {
      return null;
    }
    @Override public void unbind(android.content.ComponentName name) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.topjohnwu.superuser.internal.IRootServiceManager
  {
    /** Construct the stub at attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.topjohnwu.superuser.internal.IRootServiceManager interface,
     * generating a proxy if needed.
     */
    public static com.topjohnwu.superuser.internal.IRootServiceManager asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.topjohnwu.superuser.internal.IRootServiceManager))) {
        return ((com.topjohnwu.superuser.internal.IRootServiceManager)iin);
      }
      return new com.topjohnwu.superuser.internal.IRootServiceManager.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      if (code == INTERFACE_TRANSACTION) {
        reply.writeString(descriptor);
        return true;
      }
      switch (code)
      {
        case TRANSACTION_broadcast:
        {
          int _arg0;
          _arg0 = data.readInt();
          this.broadcast(_arg0);
          break;
        }
        case TRANSACTION_stop:
        {
          android.content.ComponentName _arg0;
          _arg0 = _Parcel.readTypedObject(data, android.content.ComponentName.CREATOR);
          int _arg1;
          _arg1 = data.readInt();
          this.stop(_arg0, _arg1);
          break;
        }
        case TRANSACTION_connect:
        {
          android.os.IBinder _arg0;
          _arg0 = data.readStrongBinder();
          this.connect(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_bind:
        {
          android.content.Intent _arg0;
          _arg0 = _Parcel.readTypedObject(data, android.content.Intent.CREATOR);
          android.os.IBinder _result = this.bind(_arg0);
          reply.writeNoException();
          reply.writeStrongBinder(_result);
          break;
        }
        case TRANSACTION_unbind:
        {
          android.content.ComponentName _arg0;
          _arg0 = _Parcel.readTypedObject(data, android.content.ComponentName.CREATOR);
          this.unbind(_arg0);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements com.topjohnwu.superuser.internal.IRootServiceManager
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      @Override public void broadcast(int uid) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(uid);
          boolean _status = mRemote.transact(Stub.TRANSACTION_broadcast, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void stop(android.content.ComponentName name, int uid) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, name, 0);
          _data.writeInt(uid);
          boolean _status = mRemote.transact(Stub.TRANSACTION_stop, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void connect(android.os.IBinder binder) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongBinder(binder);
          boolean _status = mRemote.transact(Stub.TRANSACTION_connect, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public android.os.IBinder bind(android.content.Intent intent) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.os.IBinder _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, intent, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_bind, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readStrongBinder();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void unbind(android.content.ComponentName name) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, name, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_unbind, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_broadcast = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_stop = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_connect = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_bind = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_unbind = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
  }
  /** @hide */
  public static final java.lang.String DESCRIPTOR = "com.topjohnwu.superuser.internal.IRootServiceManager";
  public void broadcast(int uid) throws android.os.RemoteException;
  public void stop(android.content.ComponentName name, int uid) throws android.os.RemoteException;
  public void connect(android.os.IBinder binder) throws android.os.RemoteException;
  public android.os.IBinder bind(android.content.Intent intent) throws android.os.RemoteException;
  public void unbind(android.content.ComponentName name) throws android.os.RemoteException;
  /** @hide */
  static class _Parcel {
    static private <T> T readTypedObject(
        android.os.Parcel parcel,
        android.os.Parcelable.Creator<T> c) {
      if (parcel.readInt() != 0) {
          return c.createFromParcel(parcel);
      } else {
          return null;
      }
    }
    static private <T extends android.os.Parcelable> void writeTypedObject(
        android.os.Parcel parcel, T value, int parcelableFlags) {
      if (value != null) {
        parcel.writeInt(1);
        value.writeToParcel(parcel, parcelableFlags);
      } else {
        parcel.writeInt(0);
      }
    }
  }
}
