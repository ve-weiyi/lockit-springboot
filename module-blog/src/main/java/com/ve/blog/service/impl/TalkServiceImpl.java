package com.ve.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ve.blog.dao.CommentDao;
import com.ve.blog.dto.CommentCountDTO;
import com.ve.blog.dto.TalkBackDTO;
import com.ve.blog.dto.TalkDTO;
import com.ve.blog.entity.Talk;
import com.ve.blog.exception.BizException;
import com.ve.blog.service.RedisService;
import com.ve.blog.service.TalkService;
import com.ve.blog.dao.TalkDao;
import com.ve.blog.util.*;
import com.ve.blog.vo.ConditionVO;
import com.ve.blog.vo.PageResult;
import com.ve.blog.vo.TalkVO;
import com.ve.blog.constant.RedisPrefixConst;
import com.ve.blog.enums.TalkStatusEnum;
import com.ve.blog.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 说说服务
 *
 * @author yezhiqiu
 * @date 2022/01/23
 */
@Service
public class TalkServiceImpl extends ServiceImpl<TalkDao, Talk> implements TalkService {
    @Autowired
    private TalkDao talkDao;
    @Autowired
    private CommentDao commentDao;
    @Autowired
    private RedisService redisService;

    @Override
    public List<String> listHomeTalks() {
        // 查询最新10条说说
        return talkDao.selectList(new LambdaQueryWrapper<Talk>()
                        .eq(Talk::getStatus, TalkStatusEnum.PUBLIC.getStatus())
                        .orderByDesc(Talk::getIsTop)
                        .orderByDesc(Talk::getId)
                        .last("limit 10"))
                .stream()
                .map(item -> item.getContent().length() > 200 ? HTMLUtils.deleteHMTLTag(item.getContent().substring(0, 200)) : HTMLUtils.deleteHMTLTag(item.getContent()))
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<TalkDTO> listTalks() {
        // 查询说说总量
        Integer count = talkDao.selectCount((new LambdaQueryWrapper<Talk>()
                .eq(Talk::getStatus, TalkStatusEnum.PUBLIC.getStatus())));
        if (count == 0) {
            return new PageResult<>();
        }
        // 分页查询说说
        List<TalkDTO> talkDTOList = talkDao.listTalks(PageUtils.getLimitCurrent(), PageUtils.getSize());
        // 查询说说评论量
        List<Integer> talkIdList = talkDTOList.stream()
                .map(TalkDTO::getId)
                .collect(Collectors.toList());
        Map<Integer, Integer> commentCountMap = commentDao.listCommentCountByTopicIds(talkIdList)
                .stream()
                .collect(Collectors.toMap(CommentCountDTO::getId, CommentCountDTO::getCommentCount));
        // 查询说说点赞量
        Map<String, Object> likeCountMap = redisService.hGetAll(RedisPrefixConst.TALK_LIKE_COUNT);
        talkDTOList.forEach(item -> {
            item.setLikeCount((Integer) likeCountMap.get(item.getId().toString()));
            item.setCommentCount(commentCountMap.get(item.getId()));
            // 转换图片格式
            if (Objects.nonNull(item.getImages())) {
                item.setImgList(CommonUtils.castList(JSON.parseObject(item.getImages(), List.class), String.class));
            }
        });
        return new PageResult<>(talkDTOList, count);
    }

    @Override
    public TalkDTO getTalkById(Integer talkId) {
        // 查询说说信息
        TalkDTO talkDTO = talkDao.getTalkById(talkId);
        if (Objects.isNull(talkDTO)) {
            throw new BizException("说说不存在");
        }
        // 查询说说点赞量
        talkDTO.setLikeCount((Integer) redisService.hGet(RedisPrefixConst.TALK_LIKE_COUNT, talkId.toString()));
        // 转换图片格式
        if (Objects.nonNull(talkDTO.getImages())) {
            talkDTO.setImgList(CommonUtils.castList(JSON.parseObject(talkDTO.getImages(), List.class), String.class));
        }
        return talkDTO;
    }

    @Override
    public void saveTalkLike(Integer talkId) {
        // 判断是否点赞
        String talkLikeKey = RedisPrefixConst.TALK_USER_LIKE + UserUtils.getLoginUser().getUserInfoId();
        if (redisService.sIsMember(talkLikeKey, talkId)) {
            // 点过赞则删除说说id
            redisService.sRemove(talkLikeKey, talkId);
            // 说说点赞量-1
            redisService.hDecr(RedisPrefixConst.TALK_LIKE_COUNT, talkId.toString(), 1L);
        } else {
            // 未点赞则增加说说id
            redisService.sAdd(talkLikeKey, talkId);
            // 说说点赞量+1
            redisService.hIncr(RedisPrefixConst.TALK_LIKE_COUNT, talkId.toString(), 1L);
        }
    }

    @Override
    public void saveOrUpdateTalk(TalkVO talkVO) {
        Talk talk = BeanCopyUtils.copyObject(talkVO, Talk.class);
        talk.setUserId(UserUtils.getLoginUser().getUserInfoId());
        this.saveOrUpdate(talk);
    }

    @Override
    public void deleteTalks(List<Integer> talkIdList) {
        talkDao.deleteBatchIds(talkIdList);
    }

    @Override
    public PageResult<TalkBackDTO> listBackTalks(ConditionVO conditionVO) {
        // 查询说说总量
        Integer count = talkDao.selectCount(new LambdaQueryWrapper<Talk>()
                .eq(Objects.nonNull(conditionVO.getStatus()), Talk::getStatus, conditionVO.getStatus()));
        if (count == 0) {
            return new PageResult<>();
        }
        // 分页查询说说
        List<TalkBackDTO> talkDTOList = talkDao.listBackTalks(PageUtils.getLimitCurrent(), PageUtils.getSize(), conditionVO);
        talkDTOList.forEach(item -> {
            // 转换图片格式
            if (Objects.nonNull(item.getImages())) {
                item.setImgList(CommonUtils.castList(JSON.parseObject(item.getImages(), List.class), String.class));
            }
        });
        return new PageResult<>(talkDTOList, count);
    }

    @Override
    public TalkBackDTO getBackTalkById(Integer talkId) {
        TalkBackDTO talkBackDTO = talkDao.getBackTalkById(talkId);
        // 转换图片格式
        if (Objects.nonNull(talkBackDTO.getImages())) {
            talkBackDTO.setImgList(CommonUtils.castList(JSON.parseObject(talkBackDTO.getImages(), List.class), String.class));
        }
        return talkBackDTO;
    }

}




