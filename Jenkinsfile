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
            sh "chmod +x gradlew && ./gradlew javadoc check connectedCheck"
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
  def flavors = ['base', 'objectServer']
  for (def i = 0; i < flavors.size(); i++) {
    def flavor = flavors[i]
    sh """set -xe
      cd realm/realm-library/build/outputs/aar
      unzip realm-android-library-${flavor}-release.aar -d unzipped${flavor}
      find \$ANDROID_HOME -name dx | sort -r | head -n 1 > dx
      \$(cat dx) --dex --output=temp${flavor}.dex unzipped${flavor}/classes.jar
      cat temp${flavor}.dex | head -c 92 | tail -c 4 | hexdump -e '1/4 \"%d\"' > methods${flavor}
    """

    def methods = readFile("realm/realm-library/build/outputs/aar/methods${flavor}")
    sendMetrics('methods', methods, ['flavor':flavor])

    def aarFile = findFiles(glob: "realm/realm-library/build/outputs/aar/realm-android-library-${flavor}-release.aar")[0]
    sendMetrics('aar_size', aarFile.length as String, ['flavor':flavor])

    def soFiles = findFiles(glob: "realm/realm-library/build/outputs/aar/unzipped${flavor}/jni/*/librealm-jni.so")
    for (def j = 0; j < soFiles.size(); j++) {
        def soFile = soFiles[j]
        def abiName = soFile.path.tokenize('/')[-2]
        def libSize = soFile.length as String
        sendMetrics('abi_size', libSize, ['flavor':flavor, 'type':abiName])
    }
  }
}

def gradle(String commands) {
  sh "chmod +x gradlew && ./gradlew ${commands} --stacktrace"
}

def gradle(String relativePath, String commands) {
  sh "cd ${relativePath} && chmod +x gradlew && ./gradlew ${commands} --stacktrace"
}