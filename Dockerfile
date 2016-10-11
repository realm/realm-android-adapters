FROM ubuntu:16.04

# Locales
RUN locale-gen en_US.UTF-8
ENV LANG "en_US.UTF-8"
ENV LANGUAGE "en_US.UTF-8"
ENV LC_ALL "en_US.UTF-8"

# Set the environment variables
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV ANDROID_HOME /opt/android-sdk-linux
# Need by cmake
ENV PATH ${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools
ENV PATH ${PATH}:${NDK_HOME}
ENV NDK_CCACHE /usr/bin/ccache

# The 32 bit binaries because aapt requires it
# `file` is need by the script that creates NDK toolchains
# Keep the packages in alphabetical order to make it easy to avoid duplication
RUN DEBIAN_FRONTEND=noninteractive dpkg --add-architecture i386 \
    && apt-get update -qq \
    && apt-get install -y bsdmainutils \
                          build-essential \
                          ccache \
                          curl \
                          file \
                          git \
                          libc6:i386 \
                          libgcc1:i386 \
                          libncurses5:i386 \
                          libstdc++6:i386 \
                          libz1:i386 \
                          openjdk-8-jdk-headless \
                          s3cmd \
                          unzip \
                          wget \
                          zip \
    && apt-get clean

# Install the Android SDK
RUN cd /opt && \
    wget -q https://dl.google.com/android/repository/tools_r25.1.7-linux.zip -O android-tools-linux.zip && \
    unzip android-tools-linux.zip -d ${ANDROID_HOME} && \
    rm -f android-tools-linux.zip

# Grab what's needed in the SDK
# ↓ updates tools to at least 25.1.7, but that prints 'Nothing was installed' (so I don't check the outputs).
RUN echo y | android update sdk --no-ui --all --filter tools > /dev/null
RUN echo y | android update sdk --no-ui --all --filter platform-tools | grep 'package installed'
RUN echo y | android update sdk --no-ui --all --filter build-tools-24.0.3 | grep 'package installed'
RUN echo y | android update sdk --no-ui --all --filter extra-android-m2repository | grep 'package installed'
RUN echo y | android update sdk --no-ui --all --filter android-24 | grep 'package installed'

# Make the SDK universally readable
RUN chmod -R a+rX ${ANDROID_HOME}