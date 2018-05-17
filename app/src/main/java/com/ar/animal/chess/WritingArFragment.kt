package com.ar.animal.chess

import android.Manifest
import com.google.ar.sceneform.ux.ArFragment


class WritingArFragment : ArFragment() {
    override fun getAdditionalPermissions(): Array<String> {
        val additionalPermissions = super.getAdditionalPermissions()
        val permissionLength = additionalPermissions.size
        val permissions = arrayOfNulls<String>(permissionLength + 1)
        permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (permissionLength > 0) {
            System.arraycopy(additionalPermissions, 0, permissions, 1, additionalPermissions.size)
        }
        return permissions.requireNoNulls()
    }
}