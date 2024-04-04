package org.nrg.testing.xnat.performance.actions

import org.nrg.testing.dicom.transform.LocallyCacheableDicomTransformation
import org.nrg.testing.xnat.conf.Settings
import org.nrg.testing.xnat.performance.CheckablePerformanceResult
import org.nrg.testing.xnat.performance.PerformanceStateHelper
import org.nrg.testing.xnat.performance.charting.PerformanceCharter
import org.nrg.testing.xnat.performance.charting.RequestedComparison
import org.nrg.testing.xnat.performance.persistence.CheckablePerformanceEntry
import org.nrg.testing.xnat.performance.validator.PerformanceValidator
import org.nrg.xnat.pogo.users.User

trait CheckablePerformanceWorkflow<
        T extends CheckablePerformanceWorkflow<T, V>,
        V extends CheckablePerformanceEntry<V>> {

    Runnable setup
    String identifier
    PerformanceUserProvider userProvider
    String title
    List<RequestedComparison> requestedComparisons = []
    def validator // typing this as PerformanceValidator<V> produces bad class file

    T withSetup(Runnable setup) {
        setSetup(setup)
        this as T
    }

    T withSetup(LocallyCacheableDicomTransformation setup) {
        withSetup({
            setup.build()
        })
    }

    T withUserProvider(PerformanceUserProvider userProvider) {
        this.userProvider = userProvider
        this as T
    }

    T asUser(User user) {
        withUserProvider(new FixedUserProvider(user))
    }

    T validateUsing(PerformanceValidator<V> validator) {
        setValidator(validator)
        this as T
    }

    T title(String title) {
        setTitle(title)
        this as T
    }

    T compareTo(String otherTestId, String otherTestDescription, String otherTestShortKey) {
        requestedComparisons << new RequestedComparison(
                otherTestId: otherTestId,
                otherTestDescription: otherTestDescription,
                otherTestShortKey: otherTestShortKey
        )
        this as T
    }

    CheckablePerformanceResult run(PerformanceStateHelper stateHelper) {
        if (setup != null) {
            setup.run()
        }
        userProvider?.setup(stateHelper)
        final V checkableEntry = produceCheckableEntry(stateHelper)
        checkableEntry.setTimestamp(System.currentTimeMillis())
        checkableEntry.setXnatVersion(Settings.XNAT_VERSION_AS_STRING)
        (validator ?: getDefaultValidator()).validate(identifier, checkableEntry)
    }

    List<PerformanceCharter<T, V>> getComparativeCharters() {
        []
    }

    abstract V produceCheckableEntry(PerformanceStateHelper stateHelper)

    abstract PerformanceValidator<V> getDefaultValidator()

    abstract PerformanceCharter<T, V> getPerformanceCharter()

}
