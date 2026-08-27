package com.farmsos.domain.model

/**
 * Application-level authorization matrix. Must stay aligned with
 * supabase/migrations RLS policies.
 *
 * OWNER: full farm access (update, delete, manage all members).
 * MANAGER: update farm details; invite WORKER members; cannot delete farm or change OWNER.
 * WORKER: read-only farm access.
 */
object FarmPermissions {
    fun canReadFarm(role: UserRole): Boolean = true

    fun canUpdateFarm(role: UserRole): Boolean = canWriteFarmStructure(role)

    fun canDeleteFarm(role: UserRole): Boolean = role == UserRole.OWNER

    fun canTransferOwnership(role: UserRole): Boolean = role == UserRole.OWNER

    fun canChangeMemberRoles(role: UserRole): Boolean = role == UserRole.OWNER

    fun canRemoveMembers(role: UserRole): Boolean = role == UserRole.OWNER

    fun canInvite(actor: UserRole, invitee: UserRole): Boolean {
        if (invitee == UserRole.OWNER) return false
        return when (actor) {
            UserRole.OWNER -> invitee == UserRole.MANAGER || invitee == UserRole.WORKER
            UserRole.MANAGER -> invitee == UserRole.WORKER
            UserRole.WORKER -> false
        }
    }
}
