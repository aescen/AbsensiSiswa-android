package id.ycmlg.absensisiswa.main.guru.home.menukelas;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import id.ycmlg.absensisiswa.R;
import id.ycmlg.absensisiswa.data.Catatan;
import id.ycmlg.absensisiswa.data.SNTPClient;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SikapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SikapFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";

    // TODO: Rename and change types of parameters
    private String namaKelas;
    private String nis;
    private String namaLengkap;
    private String catatan;

    public SikapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @param param3 Parameter 3.
     * @param param4 Parameter 4.
     * @return A new instance of fragment SikapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SikapFragment newInstance(String param1, String param2, String param3, String param4) {
        SikapFragment fragment = new SikapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3, param3);
        args.putString(ARG_PARAM4, param4);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            namaKelas = getArguments().getString(ARG_PARAM1);
            nis = getArguments().getString(ARG_PARAM2);
            namaLengkap = getArguments().getString(ARG_PARAM3);
            catatan = getArguments().getString(ARG_PARAM4);
            database = FirebaseDatabase.getInstance();
            catatanRef = database.getReference("catatan").child(namaKelas.toLowerCase().replaceAll("\\s+",""));
            dsRef = database.getReference("ds").child(namaKelas.toLowerCase().replaceAll("\\s+",""));
        }
    }

    private View root;
    private FirebaseDatabase database = null;
    private DatabaseReference catatanRef = null;
    private DatabaseReference dsRef = null;
    private EditText ed_no_induk_sikap;
    private TextView tv_no_induk_sikap;
    private TextView tv_nama_lengkap_sikap;
    private EditText ed_catatan_sikap;
    private TextView tv_catatan_sikap;
    private ImageButton ib_send_sikap;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_sikap, container, false);
        ed_no_induk_sikap = root.findViewById(R.id.ed_no_induk_sikap);
        tv_no_induk_sikap = root.findViewById(R.id.tv_no_induk_sikap);
        tv_nama_lengkap_sikap = root.findViewById(R.id.tv_nama_lengkap_sikap);
        ed_catatan_sikap = root.findViewById(R.id.ed_catatan_sikap);
        tv_catatan_sikap = root.findViewById(R.id.tv_catatan_sikap);
        ib_send_sikap = root.findViewById(R.id.ib_send_sikap);

        if(nis != null && namaLengkap != null && catatan != null){
            ed_no_induk_sikap.setVisibility(View.GONE);
            tv_no_induk_sikap.setVisibility(View.VISIBLE);
            ed_catatan_sikap.setVisibility(View.GONE);
            tv_catatan_sikap.setVisibility(View.VISIBLE);
            tv_no_induk_sikap.setText(nis);
            tv_nama_lengkap_sikap.setText(namaLengkap);
            tv_catatan_sikap.setText(catatan);
            ib_send_sikap.setVisibility(View.GONE);
        } else {
            ib_send_sikap.setOnClickListener(view -> {
                if(ed_no_induk_sikap.getText().toString().trim().length() == 0) ed_no_induk_sikap.setError("NIS kosong!");
                if(ed_catatan_sikap.getText().toString().trim().length() == 0) ed_catatan_sikap.setError("Catatan kosong!");
            });

            ed_no_induk_sikap.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void afterTextChanged(Editable editable) {
                    nis = ed_no_induk_sikap.getText().toString().trim();
                    if(nis.length() > 3 && TextUtils.isDigitsOnly(nis)){
                        SNTPClient sntpClient = null;
                        sntpClient.getDate(Calendar.getInstance().getTimeZone(), new SNTPClient.Listener() {
                            @Override
                            public void onTimeReceived(String rawDate) {
                                final String tgl = rawDate.substring(0, rawDate.indexOf("T"));
                                final String wkt = rawDate.substring(rawDate.indexOf("T") + 1, rawDate.indexOf("+"));
                                dsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        boolean userFound = false;
                                        for (DataSnapshot userChild: snapshot.getChildren()) {
                                            if(nis.contentEquals(userChild.getKey())){
                                                namaLengkap = userChild.child("nama").getValue().toString();
                                                userFound = true;
                                                break;
                                            }
                                        }
                                        if(userFound){
                                            final LinearLayout pb_ll_sikap = root.findViewById(R.id.pb_ll_sikap);
                                            tv_nama_lengkap_sikap.setText(namaLengkap);
                                            ed_catatan_sikap.addTextChangedListener(new TextWatcher() {
                                                @Override
                                                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                                }

                                                @Override
                                                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                                }

                                                @Override
                                                public void afterTextChanged(Editable editable) {
                                                    catatan = ed_catatan_sikap.getText().toString();
                                                    ib_send_sikap.setOnClickListener(null);
                                                    ib_send_sikap.setOnClickListener(view -> {
                                                        if(catatan.trim().length() > 0){
                                                            pb_ll_sikap.setVisibility(View.VISIBLE);
                                                            Catatan newCatatan = new Catatan();
                                                            newCatatan.nis = nis;
                                                            newCatatan.nm = namaLengkap;
                                                            newCatatan.plgrn = null;
                                                            newCatatan.skp = catatan;
                                                            newCatatan.tgl = tgl;
                                                            newCatatan.wkt = wkt;
                                                            catatanRef.child(tgl).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    if (snapshot.getValue() != null){
                                                                        long arrTotal = snapshot.getChildrenCount();
                                                                        catatanRef.child(tgl).child(String.valueOf(arrTotal)).setValue(newCatatan);
                                                                    } else {
                                                                        catatanRef.child(tgl).child(String.valueOf(0)).setValue(newCatatan);
                                                                    }
                                                                    ed_catatan_sikap.setText("");
                                                                    pb_ll_sikap.setVisibility(View.GONE);
                                                                    Toast.makeText(requireContext(), "Penyimpanan selesai!", Toast.LENGTH_SHORT).show();
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {
                                                                    ed_catatan_sikap.setText("");
                                                                    pb_ll_sikap.setVisibility(View.GONE);
                                                                    Toast.makeText(requireContext(), "Penyimpanan gagal!", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        } else {
                                                            Toast.makeText(requireContext(), "Isi catatan kosong!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            });
                                        } else {
                                            ed_no_induk_sikap.setError("NIS tidak ada!");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                            @Override
                            public void onError(Exception ex) {

                            }
                        });
                    } else if(nis.trim().length() > 0 && nis.trim().length() <= 3) {
                        ed_no_induk_sikap.setError("NIS tidak ada!");
                    }
                }
            });
        }


        return root;
    }

}