import java.util.Arrays;

class City extends Place implements Comparable
{
	protected Integer areaSize;

	public City(String name, Integer areaSize) {
		super(name);
		this.areaSize = areaSize;
	}

	public void setAreaSize(Integer areaSize) {
		this.areaSize = areaSize;
	}

	public Integer getAreaSize() {
		return areaSize;
	}

	@Override
	public int compareTo(Object object) {
		if (this == object)
            return 0;

		if (object == null)
			throw new NullPointerException();

		if (this.getClass() != object.getClass())
			throw new ClassCastException();

		City city = (City)object;
		return this.areaSize.compareTo(city.areaSize) + this.name.compareTo(city.name);
	}

	@Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (object == null)
            return false;

        if (this.getClass() != object.getClass())
            return false;

        City city = (City)object;
        if (!this.name.equals(city.name) || !this.areaSize.equals(city.areaSize))
            return false;

        return true;
    }

    @Override
	public int hashCode() {
		return Arrays.deepHashCode((Object[])new Object[]{name, areaSize});
	}

    @Override
    public String toString() {
        return "Имя города: " + name + "\n" + "Площадь: " + areaSize;
    }
}
