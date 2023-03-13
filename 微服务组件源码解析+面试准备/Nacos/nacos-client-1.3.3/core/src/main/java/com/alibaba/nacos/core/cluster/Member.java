/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.core.cluster;

import com.alibaba.nacos.core.utils.ApplicationUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Cluster member node.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class Member implements Comparable<Member>, Cloneable {

    private String ip;

    private int port = -1;

    private volatile NodeState state = NodeState.UP;

    private Map<String, Object> extendInfo = Collections.synchronizedMap(new TreeMap<>());

    private String address = "";

    private transient int failAccessCnt = 0;

    public Member() {
        String prefix = "nacos.core.member.meta.";
        extendInfo.put(MemberMetaDataConstants.SITE_KEY,
                ApplicationUtils.getProperty(prefix + MemberMetaDataConstants.SITE_KEY, "unknow"));
        extendInfo.put(MemberMetaDataConstants.AD_WEIGHT,
                ApplicationUtils.getProperty(prefix + MemberMetaDataConstants.AD_WEIGHT, "0"));
        extendInfo.put(MemberMetaDataConstants.WEIGHT,
                ApplicationUtils.getProperty(prefix + MemberMetaDataConstants.WEIGHT, "1"));
    }

    public static MemberBuilder builder() {
        return new MemberBuilder();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public NodeState getState() {
        return state;
    }

    public void setState(NodeState state) {
        this.state = state;
    }

    public Map<String, Object> getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(Map<String, Object> extendInfo) {
        this.extendInfo.putAll(extendInfo);
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getAddress() {
        if (StringUtils.isBlank(address)) {
            address = ip + ":" + port;
        }
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Object getExtendVal(String key) {
        return extendInfo.get(key);
    }

    public void setExtendVal(String key, Object value) {
        extendInfo.put(key, value);
    }

    public boolean check() {
        return StringUtils.isNoneBlank(ip, address) && port != -1;
    }

    public int getFailAccessCnt() {
        return failAccessCnt;
    }

    public void setFailAccessCnt(int failAccessCnt) {
        this.failAccessCnt = failAccessCnt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Member that = (Member) o;
        if (StringUtils.isAnyBlank(address, that.address)) {
            return port == that.port && StringUtils.equals(ip, that.ip);
        }
        return StringUtils.equals(address, that.address);
    }

    @Override
    public String toString() {
        return "Member{" + "ip='" + ip + '\'' + ", port=" + port + ", state=" + state + ", extendInfo=" + extendInfo
                + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }

    @Override
    public int compareTo(Member o) {
        return getAddress().compareTo(o.getAddress());
    }

    public static final class MemberBuilder {

        private String ip;

        private int port;

        private NodeState state;

        private Map<String, String> extendInfo = Collections.synchronizedMap(new TreeMap<>());

        private MemberBuilder() {
        }

        public MemberBuilder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public MemberBuilder port(int port) {
            this.port = port;
            return this;
        }

        public MemberBuilder state(NodeState state) {
            this.state = state;
            return this;
        }

        public MemberBuilder extendInfo(Map<String, String> extendInfo) {
            this.extendInfo.putAll(extendInfo);
            return this;
        }

        /**
         * build Member.
         *
         * @return {@link Member}
         */
        public Member build() {
            Member serverNode = new Member();
            if (Objects.nonNull(this.extendInfo)) {
                serverNode.extendInfo.putAll(this.extendInfo);
            }
            serverNode.state = this.state;
            serverNode.ip = this.ip;
            serverNode.port = this.port;
            serverNode.address = this.ip + ":" + this.port;
            return serverNode;
        }
    }

}
