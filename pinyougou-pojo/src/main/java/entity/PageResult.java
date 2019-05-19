package entity;
import java.io.Serializable;
import java.util.List;
/**
 * 分页结果封装对象
 * @author Administrator
 *
 */
public class PageResult implements Serializable {
	private Long total;//总记录数------------------------------------------------------PageResult=response.total
	private List rows; //当前页结果集//将Page<Brand> extends ArrayList 中对应值存进来----PageResult=response.total
					   //如果List中不写泛型,那么 编译的时候会很慢,但运行的时候 不会慢
	public PageResult(Long total, List rows) {
		super();
		this.total = total;
		this.rows = rows;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public List getRows() {
		return rows;
	}

	public void setRows(List rows) {
		this.rows = rows;
	}

}
