package com.example.budgetmanager;


import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

import Model.Data;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExpenseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExpenseFragment extends Fragment {

    //Firebase Database
    private FirebaseAuth mAuth;
    private DatabaseReference mExpenseDatabase;

    //Recycler View
    private RecyclerView recyclerView;
    private TextView expenseSumResult;//Textview displaying sum of total expense


    //Edt Data amount
    private EditText edtAmount;
    private EditText edtType;
    private EditText edtNote;

    private Button btnUpdate;
    private Button btnDelete;


    //Declaring Data Item variables...
    private String type;
    private String note;
    private int amount;

    private String post_key;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FirebaseRecyclerAdapter<Data, MyViewHolder> adapter;

    public ExpenseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ExpenseFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExpenseFragment newInstance(String param1, String param2) {
        ExpenseFragment fragment = new ExpenseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myview = inflater.inflate(R.layout.fragment_expense, container, false);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();

        mExpenseDatabase = FirebaseDatabase.getInstance().getReference().child("ExpenseDatabase").child(uid);

        expenseSumResult=myview.findViewById(R.id.expense_txt_result);

        recyclerView = myview.findViewById(R.id.recycler_id_expense);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int expenseSum=0;

                for (DataSnapshot mysnapshot:snapshot.getChildren()){//adding total value of expense fragment

                    Data data=mysnapshot.getValue(Data.class);
                    expenseSum+=data.getAmount();
                    String strExpensesum=String.valueOf(expenseSum);

                    expenseSumResult.setText("GH₵"+ " " +strExpensesum+".00");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return myview;
    }

    @Override

    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(mExpenseDatabase, Data.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {

            public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_recycler_data, parent, false));
            }

            protected void onBindViewHolder(MyViewHolder viewHolder, int position, @NonNull Data model) {
                viewHolder.setAmount(model.getAmount());
                viewHolder.setType(model.getType());
                viewHolder.setNote(model.getNote());
                viewHolder.setDate(model.getDate());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        post_key=getRef(position).getKey();
                        type=model.getType();
                        note=model.getNote();
                        amount=model.getAmount();


                        updateDataItem();
                    }
                });

            }

        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();


}


       private static class MyViewHolder extends RecyclerView.ViewHolder{


            View mView;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                mView = itemView;
            }

           private void setType(String type) {
                TextView mType = mView.findViewById(R.id.type_txt_expense);
                mType.setText(type);
            }

            private void setNote(String note) {

                TextView mNote = mView.findViewById(R.id.note_txt_expense);
                mNote.setText(note);
            }

            private void setDate(String date) {
                TextView mDate = mView.findViewById(R.id.date_txt_expense);
                mDate.setText(date);
            }

            private void setAmount(int amount) {
                TextView mAmount = mView.findViewById(R.id.amount_txt_expense);
                String stamount = String.valueOf(amount);
                mAmount.setText("GH₵"+ " " + stamount+".00");

            }


        }

        private void updateDataItem(){

            AlertDialog.Builder mydialog=new AlertDialog.Builder(getActivity());
            LayoutInflater inflater=LayoutInflater.from(getActivity());
            View myview=inflater.inflate(R.layout.update_data_item,null);

            mydialog.setView(myview);

            edtAmount=myview.findViewById(R.id.amount_edt);
            edtNote=myview.findViewById(R.id.note_edt);
            edtType=myview.findViewById(R.id.type_edt);


            //Set data to edit text here
            edtType.setText(type);
            edtType.setSelection(type.length());

            edtNote.setText(note);
            edtNote.setSelection(note.length());

            edtAmount.setText(String.valueOf(amount));
            edtAmount.setSelection(String.valueOf(amount).length());




            btnUpdate=myview.findViewById(R.id.btn_update);
            btnDelete=myview.findViewById(R.id.btn_UPD_delete);

            final AlertDialog dialog=mydialog.create();

            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    type=edtType.getText().toString().trim();
                    note=edtNote.getText().toString().trim();

                    String mdamount=String.valueOf(amount);
                    mdamount=edtAmount.getText().toString().trim();

                    int myAmount=Integer.parseInt(mdamount);

                    String mDate= DateFormat.getDateInstance().format(new Date());

                    Data data= new Data(myAmount,type,note,post_key,mDate);

                   mExpenseDatabase.child(post_key).setValue(data);

                    dialog.dismiss();


                }
            });

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mExpenseDatabase.child(post_key).removeValue();

                    dialog.dismiss();

                }
            });

            dialog.show();


        }



}







