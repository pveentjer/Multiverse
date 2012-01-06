package org.multiverse.stms.gamma;

import org.multiverse.MultiverseConstants;

/**
 * Contains the constants for the {@link GammaStm}.
 *
 * @author Peter Veentjer.
 */
public interface GammaConstants extends MultiverseConstants {

    int FAILURE = 0;
    int MASK_SUCCESS = 1;
    int MASK_UNREGISTERED = MASK_SUCCESS * 2;
    int MASK_CONFLICT = MASK_UNREGISTERED * 2;

    int REGISTRATION_DONE = 0;
    int REGISTRATION_NOT_NEEDED = 1;
    int REGISTRATION_NONE = 2;

    int TRANLOCAL_CONSTRUCTING = 1;
    int TRANLOCAL_WRITE = 2;
    int TRANLOCAL_COMMUTING = 3;
    int TRANLOCAL_READ = 4;

    int TX_ACTIVE = 1;
    int TX_PREPARED = 2;
    int TX_ABORTED = 3;
    int TX_COMMITTED = 4;

    int TYPE_INT = 1;
    int TYPE_LONG = 2;
    int TYPE_DOUBLE = 3;
    int TYPE_BOOLEAN = 4;
    int TYPE_REF = 5;

    int TRANSACTIONTYPE_LEAN_MONO = 1;
    int TRANSACTIONTYPE_LEAN_FIXED_LENGTH = 2;
    int TRANSACTIONTYPE_FAT_MONO = 3;
    int TRANSACTIONTYPE_FAT_FIXED_LENGTH = 4;
    int TRANSACTIONTYPE_FAT_VARIABLE_LENGTH = 5;

    int VERSION_UNCOMMITTED = 0;
}
