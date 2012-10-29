package factory.cms;

import java.util.Date;

import models.cms.VoteQuestion;
import models.cms.VoteType;
import util.DateHelper;

import com.uhuila.common.constants.DeletedStatus;

import factory.FactoryBoy;
import factory.ModelFactory;

public class VoteQuestionFactory extends ModelFactory<VoteQuestion> {

    @Override
    public VoteQuestion define() {
        VoteQuestion vq = new VoteQuestion();
        vq.content = "内容" + FactoryBoy.sequence(VoteQuestion.class);
        vq.deleted = DeletedStatus.UN_DELETED;
        vq.effectiveAt = DateHelper.beforeDays(new Date(), 1);
        vq.expireAt = DateHelper.afterDays(new Date(), 1);
        vq.type = VoteType.INQUIRY;
        vq.answer1 = "A";
        vq.answer2 = "B";
        vq.answer3 = "C";
        vq.answer4 = "D";
        vq.correctAnswer = "A";
        return vq;
    }

}
