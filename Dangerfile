tag_name = ENV['BITRISE_GIT_TAG']
is_release = tag_name != nil && tag_name != ""

message "Tag Name: #{tag_name}, Is Release: #{is_release}"

# Warn when there is a big PR
warn("Big PR") if git.lines_of_code > 1000

# Code coverage
jacoco.minimum_project_coverage_percentage = 80
jacoco.report(is_release ? "inappmessaging/build/reports/jacoco/jacocoReleaseReport/jacocoReleaseReport.xml" :
                           "inappmessaging/build/reports/jacoco/jacocoDebugReport/jacocoDebugReport.xml",
                           fail_no_coverage_data_found: false)