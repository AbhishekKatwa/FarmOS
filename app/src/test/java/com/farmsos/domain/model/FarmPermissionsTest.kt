package com.farmsos.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FarmPermissionsTest {

    @Test
    fun ownerHasFullFarmAccess() {
        val role = UserRole.OWNER
        assertTrue(FarmPermissions.canReadFarm(role))
        assertTrue(FarmPermissions.canUpdateFarm(role))
        assertTrue(FarmPermissions.canDeleteFarm(role))
        assertTrue(FarmPermissions.canTransferOwnership(role))
        assertTrue(FarmPermissions.canChangeMemberRoles(role))
        assertTrue(FarmPermissions.canRemoveMembers(role))
        assertTrue(FarmPermissions.canInvite(role, UserRole.MANAGER))
        assertTrue(FarmPermissions.canInvite(role, UserRole.WORKER))
        assertFalse(FarmPermissions.canInvite(role, UserRole.OWNER))
    }

    @Test
    fun managerCanUpdateButNotDeleteOrManageOwners() {
        val role = UserRole.MANAGER
        assertTrue(FarmPermissions.canUpdateFarm(role))
        assertFalse(FarmPermissions.canDeleteFarm(role))
        assertFalse(FarmPermissions.canTransferOwnership(role))
        assertFalse(FarmPermissions.canChangeMemberRoles(role))
        assertTrue(FarmPermissions.canInvite(role, UserRole.WORKER))
        assertFalse(FarmPermissions.canInvite(role, UserRole.MANAGER))
    }

    @Test
    fun workerIsReadOnly() {
        val role = UserRole.WORKER
        assertTrue(FarmPermissions.canReadFarm(role))
        assertFalse(FarmPermissions.canUpdateFarm(role))
        assertFalse(FarmPermissions.canDeleteFarm(role))
        assertFalse(FarmPermissions.canInvite(role, UserRole.WORKER))
    }
}
