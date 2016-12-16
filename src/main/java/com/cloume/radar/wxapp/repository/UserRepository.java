package com.cloume.radar.wxapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import me.chanjar.weixin.mp.bean.result.WxMpUser;

public interface UserRepository extends MongoRepository<WxMpUser, String> {

}
