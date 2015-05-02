
/**
 * ListAdapter of $(itemClass) 
 * Created by Liu Wei
 */
public class $(itemClass)ListAdapter extends ArrayAdapter<$(itemClass)> {

    private static class ViewHodler {
        $for(field in fields)
        $field.first $(field.second);$end()
    }

    public $(itemClass)ListAdapter(Context context, List<$(itemClass)> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.$(itemLayout), null);
            
            ViewHodler vh = new ViewHodler();
            
            $for(field in fields)
            vh.$(field.second) = ($(field.first)) v.findViewById(R.id.$(field.second));$(end)

            v.setTag(vh);
        }

        ViewHodler vh = (ViewHodler) v.getTag();

        $(itemClass) item = getItem(position);

        $for(field in fields)
        vh.$(field.second).setText(item.$(field.second));$(end)

        return v;
    }
}"""

