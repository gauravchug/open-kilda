# Copyright 2018 Telstra Open Source
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

FROM ubuntu:cosmic

ENV DEBIAN_FRONTEND noninteractive

RUN apt update \
    && apt install -y --no-install-recommends \
        software-properties-common \
        curl \
        make \
        python3-pip \
        iproute2 \
        apt-transport-https \
        net-tools \
        iperf3 \
        iputils-ping \
        openvswitch-switch \
        openvswitch-common \
    && pip3 install setuptools wheel tox \
    && rm -rfv /var/lib/apt/lists/* /tmp/* /var/tmp/*
