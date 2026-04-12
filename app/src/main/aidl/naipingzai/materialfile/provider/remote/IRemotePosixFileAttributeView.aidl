package naipingzai.materialfile.provider.remote;

import naipingzai.materialfile.provider.common.ParcelableFileTime;
import naipingzai.materialfile.provider.common.ParcelablePosixFileMode;
import naipingzai.materialfile.provider.common.PosixGroup;
import naipingzai.materialfile.provider.common.PosixUser;
import naipingzai.materialfile.provider.remote.ParcelableException;
import naipingzai.materialfile.provider.remote.ParcelableObject;

interface IRemotePosixFileAttributeView {
    ParcelableObject readAttributes(out ParcelableException exception);

    void setTimes(
        in ParcelableFileTime lastModifiedTime,
        in ParcelableFileTime lastAccessTime,
        in ParcelableFileTime createTime,
        out ParcelableException exception
    );

    void setOwner(in PosixUser owner, out ParcelableException exception);

    void setGroup(in PosixGroup group, out ParcelableException exception);

    void setMode(in ParcelablePosixFileMode mode, out ParcelableException exception);

    void setSeLinuxContext(in ParcelableObject context, out ParcelableException exception);

    void restoreSeLinuxContext(out ParcelableException exception);
}