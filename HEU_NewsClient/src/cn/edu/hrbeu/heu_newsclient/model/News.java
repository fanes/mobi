package cn.edu.hrbeu.heu_newsclient.model;

public class News {
	
	private String NewsID;
	private String Title;
	private String Category;
	private String Abstract;
	private String Provider;
	private String Datetime;
	private String StorageLoc;
	private String Link;
	
	public String getTitle() {
		return Title;
	}
	public void setTitle(String title) {
		Title = title;
	}
	public String getAbstract() {
		return Abstract;
	}
	public void setAbstract(String abstract1) {
		Abstract = abstract1;
	}
	public String getProvider() {
		return Provider;
	}
	public void setProvider(String provider) {
		Provider = provider;
	}
	public String getDatetime() {
		if(Datetime.equals(""))
			return null;
		else{
			return Datetime.substring(5,Datetime.length()-3);
		}
	}
	public void setDatetime(String datetime) {
		Datetime = datetime;
	}
	public String getNewsID() {
		return NewsID;
	}
	public void setNewsID(String newsID) {
		NewsID = newsID;
	}
	public String getCategory() {
		return Category;
	}
	public void setCategory(String category) {
		Category = category;
	}
	public String getStorageLoc() {
		return StorageLoc;
	}
	public void setStorageLoc(String storageLoc) {
		StorageLoc = storageLoc;
	}
	public String getLink() {
		return Link;
	}
	public void setLink(String link) {
		Link = link;
	}

}