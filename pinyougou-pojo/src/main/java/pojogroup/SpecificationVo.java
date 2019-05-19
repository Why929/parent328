package pojogroup;

import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;

import java.io.Serializable;
import java.util.List;

/**
 * 首先需要进行序列化:这样才能使这个对象在跨服务器之间进行二进制转化传输
 */
public class SpecificationVo implements Serializable{
    /**
     * 内容分析:主要是:两张表:1.规格表 2.规格选项表_是对象集合_不同的规格属性对象集合
     * 那么就是包含两个对象:1.规格对象 2.规格选项表集合对象
     * 1.规格表在pojo包中:specification
     * 2.规格选项表在pojo包中:specificationOption
     */
//    1.规格表对象:
    private Specification specification;
//    2.规格选项表集合对象:问题:为什么不是使用Page<SpecificationOption>接收?答:查询的时候才用那个接收
//    要点:集合名需要同entity参数结构内部 定义规格选项名一样,不然就会赋不进来值
    private List<SpecificationOption> specificationOptionList;
//    3.get/set方法


    public Specification getSpecification() {
        return specification;
    }

    public void setSpecification(Specification specification) {
        this.specification = specification;
    }

    public List<SpecificationOption> getSpecificationOptionList() {
        return specificationOptionList;
    }

    public void setSpecificationOptionList(List<SpecificationOption> specificationOptionList) {
        this.specificationOptionList = specificationOptionList;
    }
}
