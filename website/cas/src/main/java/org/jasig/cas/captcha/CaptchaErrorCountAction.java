package org.jasig.cas.captcha;


import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
 
public final class CaptchaErrorCountAction extends AbstractAction {
 
   protected Event doExecute(final RequestContext context) {

      int count;
      try {
         count = (Integer)context.getFlowScope().get("count");
      } catch (Exception e) {
         count=0;
      }
 
      count++;
 
      context.getFlowScope().put("count",count);
 
      return success();
   }
}
