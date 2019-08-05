package liquibase.license;

import liquibase.exception.ValidationErrors;
import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.changelog.ChangeSet;

/**
 *
 * This class provides a static method for verifying licenses
 *
 */
public class LicenseServiceUtils {
    private static Logger LOG = LogService.getLog(LicenseServiceUtils.class);

    public static ValidationErrors checkForValidLicense(String licenseType, ChangeSet changeSet, String changeType) {
      LicenseService licenseService = LicenseServiceFactory.getInstance().getLicenseService();
      if (licenseService == null) {
        return new ValidationErrors();
      }

      if (licenseService.licenseIsValid(licenseType)) {
        String message = String.format("Found valid license with subject '%s'",licenseType);
        LOG.info(message);
        return new ValidationErrors();
      }
      ValidationErrors validationErrors = new ValidationErrors();
      String message = "Change Set ID: " + changeSet.getId() + " Change Set Author: " + changeSet.getAuthor() + "\n";
      message += "Change Type 'pro:" + changeType + "' is not allowed without a valid Liquibase Pro License.\n";
      message += "To purchase or renew a Liquibase Pro license key please contact lbprosupport@datical.com or\n" +
                "go to https://download.liquibase.org/download/pricing";
      validationErrors.addError(message);
      return validationErrors;
    }
}
