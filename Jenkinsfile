#!groovy

import groovy.json.JsonOutput

def buildSuccess = false
try {
  node('android') {
    // Allocate a custom workspace to avoid having % in the path (it breaks ld)
    ws('/tmp/realm-android-adapters') {
      stage('SCM') {
        checkout([
          $class: 'GitSCM',
          branches: scm.branches,
          gitTool: 'native git',
          extensions: scm.extensions + [[$class: 'CleanCheckout']],
          userRemoteConfigs: scm.userRemoteConfigs
        ])
      }

      def buildEnv
      stage('Docker build') {
        buildEnv = docker.build 'realm-android-adapters:snapshot'
      }

      buildEnv.inside("-e HOME=/tmp -e _JAVA_OPTIONS=-Duser.home=/tmp --privileged -v /dev/bus/usb:/dev/bus/usb -v ${env.HOME}/gradle-cache:/tmp/.gradle -v ${env.HOME}/.android:/tmp/.android -v ${env.HOME}/ccache:/tmp/.ccache") {
        stage('Build & Test') {
          try {
            gradle 'assemble javadoc check connectedCheck'
            if (env.BRANCH_NAME == 'master') {
              stage('Collect metrics') {
                collectAarMetrics()
              }
            }
          } finally {
            storeJunitResults '**/TEST-*.xml'
            step([$class: 'LintPublisher'])
          }
        }
      }
    }
  }
  currentBuild.rawBuild.setResult(Result.SUCCESS)
  buildSuccess = true
} catch(Exception e) {
  currentBuild.rawBuild.setResult(Result.FAILURE)
  buildSuccess = false
  throw e
}


def String startLogCatCollector() {
  sh '''adb logcat -c
  adb logcat -v time > "logcat.txt" &
  echo $! > pid
  '''
  return readFile("pid").trim()
}

def stopLogCatCollector(String backgroundPid, boolean archiveLog) {
  sh "kill ${backgroundPid}"
  if (archiveLog) {
    zip([
      'zipFile': 'logcat.zip',
      'archive': true,
      'glob' : 'logcat.txt'
    ])
  }
  sh 'rm logcat.txt '
}

def sendMetrics(String metricName, String metricValue, Map<String, String> tags) {
  def tagsString = getTagsString(tags)
  withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: '5b8ad2d9-61a4-43b5-b4df-b8ff6b1f16fa', passwordVariable: 'influx_pass', usernameVariable: 'influx_user']]) {
    sh "curl -i -XPOST 'https://greatscott-pinheads-70.c.influxdb.com:8086/write?db=realm' --data-binary '${metricName},${tagsString} value=${metricValue}i' --user '${env.influx_user}:${env.influx_pass}'"
  }
}

@NonCPS
def getTagsString(Map<String, String> tags) {
  return tags.collect { k,v -> "$k=$v" }.join(',')
}

def storeJunitResults(String path) {
  step([
    $class: 'JUnitResultArchiver',
    testResults: path
  ])
}

def collectAarMetrics() {
  sh """set -xe
    cd adapters/build/outputs/aar
    unzip android-adapters-release.aar -d unzipped
    find \$ANDROID_HOME -name dx | sort -r | head -n 1 > dx
    \$(cat dx) --dex --output=temp.dex unzipped/classes.jar
    cat temp.dex | head -c 92 | tail -c 4 | hexdump -e '1/4 \"%d\"' > methods
  """

  def methods = readFile('adapters/build/outputs/aar/methods')
  sendMetrics('adapters_methods', methods, [:])

  def aarFile = findFiles(glob: 'adapters/build/outputs/aar/android-adapters-release.aar')[0]
  sendMetrics('adapters_aar_size', aarFile.length as String, [:])
}

def gradle(String commands) {
  // in order to skip auto NDK installation, delete generated local.properties file
  sh "rm -f local.properties"
  sh "chmod +x gradlew && ./gradlew ${commands} --stacktrace"
}

def gradle(String relativePath, String commands) {
  // in order to skip auto NDK installation, delete generated local.properties file
  sh "rm -f \"${relativePath}/local.properties\""
  sh "cd ${relativePath} && chmod +x gradlew && ./gradlew ${commands} --stacktrace"
}
