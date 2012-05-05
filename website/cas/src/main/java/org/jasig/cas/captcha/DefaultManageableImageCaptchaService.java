package org.jasig.cas.captcha;

import com.octo.captcha.engine.CaptchaEngine;
import com.octo.captcha.service.captchastore.CaptchaStore;
import com.octo.captcha.service.captchastore.FastHashMapCaptchaStore;
import com.octo.captcha.service.image.AbstractManageableImageCaptchaService;
import com.octo.captcha.service.image.ImageCaptchaService;

public class DefaultManageableImageCaptchaService extends AbstractManageableImageCaptchaService
  implements ImageCaptchaService
{
    
    public DefaultManageableImageCaptchaService()
    {
        super(new FastHashMapCaptchaStore(), new MyGimpyEngine(), 180,
                100000, 75000);
    }

    public DefaultManageableImageCaptchaService(
            int minGuarantedStorageDelayInSeconds, int maxCaptchaStoreSize,
            int captchaStoreLoadBeforeGarbageCollection)
    {
        super(new FastHashMapCaptchaStore(), new MyGimpyEngine(),
                minGuarantedStorageDelayInSeconds, maxCaptchaStoreSize,
                captchaStoreLoadBeforeGarbageCollection);
    }

    public DefaultManageableImageCaptchaService(CaptchaStore captchaStore,
            CaptchaEngine captchaEngine, int minGuarantedStorageDelayInSeconds,
            int maxCaptchaStoreSize, int captchaStoreLoadBeforeGarbageCollection)
    {
        super(captchaStore, captchaEngine, minGuarantedStorageDelayInSeconds,
                maxCaptchaStoreSize, captchaStoreLoadBeforeGarbageCollection);
    }
}
