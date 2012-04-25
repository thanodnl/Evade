/**
 * 
 */
package nl.thanod.evade.document;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author nilsdijk
 */
public class MergeTest
{

	private List<Document> mutations;
	private Document result;

	@BeforeClass
	public void setup()
	{
		this.mutations = new ArrayList<Document>();

		this.mutations.add(DocumentBuilder.start(1).put("Name", "Nils Dijk").make());
		this.mutations.add(DocumentBuilder.start(2).put("Age", 24).make());
		this.mutations.add(DocumentBuilder.start(3).tree("tags").put("hair", "long").make());
		this.mutations.add(DocumentBuilder.start(4).putNull("tags").make());
		this.mutations.add(DocumentBuilder.start(5).tree("tags").put("shoes", "es").put("owned", true).make());
		this.mutations.add(DocumentBuilder.start(6).tree("tags").tree("mom").put("your", "mom").make());

		Document b = this.mutations.get(0);
		for (int i = 1; i < this.mutations.size(); i++)
			b = Document.merge(b, this.mutations.get(i));
		this.result = b;
		System.out.println(this.result);
	}

	@Test
	public void anyOrder()
	{
		for (List<Document> bl : new AllOrders<Document>(this.mutations)) {
			StringBuilder sb = new StringBuilder();
			Document b = null;
			for (Document bs : bl) {
				if (b == null)
					b = bs;
				else
					b = Document.merge(b, bs);
				if (sb.length() > 0)
					sb.append(',');
				sb.append(bs.version);
			}

			Assert.assertEquals(b, this.result, "\nexpected:" + this.result + "\nbut was:" + b + "\nmergeorder:" + sb + '\n');
		}
	}
}
