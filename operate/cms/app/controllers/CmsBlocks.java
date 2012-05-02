package controllers;

import models.cms.Block;
import models.cms.BlockType;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.ModelPaginator;
import play.mvc.Controller;
import play.mvc.With;
import com.uhuila.common.constants.DeletedStatus;

@With(OperateRbac.class)
@ActiveNavigation("blocks_index")
public class CmsBlocks extends Controller {
    private static final int PAGE_SIZE = 15;

    public static void index(BlockType type) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        ModelPaginator blockPage = Block.getPage(pageNumber, PAGE_SIZE, type);

        render(blockPage, type);
    }

    @ActiveNavigation("blocks_add")
    public static void add() {
        render();
    }

    public static void create(@Valid Block block) {
        checkExpireAt(block);
        if (Validation.hasErrors()) {
            render("CmsBlocks/add.html", block);
        }
        block.deleted = DeletedStatus.UN_DELETED;
        block.create();
        index(null);
    }

    private static void checkExpireAt(Block block) {
        if (block.effectiveAt != null && block.expireAt != null && block.expireAt.before(block.effectiveAt)) {
            Validation.addError("block.expireAt", "validation.beforeThanEffectiveAt");
        }
    }

    public static void edit(Long id) {
        Block block = Block.findById(id);
        render(block);
    }

    public static void update(Long id, Block block) {
        checkExpireAt(block);
        if (Validation.hasErrors()) {
            for (String key : validation.errorsMap().keySet()) {
                Logger.warn("validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
            }
            render("CmsBlocks/edit.html", block);
        }

        Block.update(id, block);

        index(null);
    }

    public static void delete(Long id) {
        Block.delete(id);
        index(null);
    }
}
