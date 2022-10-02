package com.example.budgetmanager;


import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import Model.Data;

/**
 * A simple {@link Fragment} subclass.
 //Use the {@link DashboardFragment#//newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment {

    //Graph

    BarChart barChart;
    BarData barData;
    BarDataSet barDataSet;
    ArrayList barEntries;
    final String[] date=new String[10000000];

    //Floating Button
    private FloatingActionButton fab_main_btn;
    private FloatingActionButton fab_income_btn;
    private FloatingActionButton fab_expense_btn;

    //Floating button textView..
    private TextView fab_income_txt;
    private TextView fab_expense_txt;

    //boolean
    private boolean isOpen=false;

    //Animation
    private Animation FadeOpen,FadeClose,Rob,Rof;


    //Calculate for total income and expense in dashboard
    private TextView totalIncomeResult;
    private  TextView totalExpenseResult;
    private TextView totalBalanceResult;


     int balance;
     int totalsumexpense=0;
     int totalsumincome=0;


    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase;
    private DatabaseReference mExpenseDatabase;

    //Recycler View...
    private RecyclerView mRecyclerIncome;
    private RecyclerView mRecyclerExpense;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    //private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    //private String mParam1;
    //private String mParam2;

    /** public DashboardFragment() {
     // Required empty public constructor
     }*/

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DashboardFragment.
     */
    // TODO: Rename and change types and number of parameters
    /**public static DashboardFragment newInstance(String param1, String param2) {
     DashboardFragment fragment = new DashboardFragment();
     Bundle args = new Bundle();
     args.putString(ARG_PARAM1, param1);
     args.putString(ARG_PARAM2, param2);
     fragment.setArguments(args);
     return fragment;
     }
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**if (getArguments() != null) {
         mParam1 = getArguments().getString(ARG_PARAM1);
         mParam2 = getArguments().getString(ARG_PARAM2);
         }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myview =  inflater.inflate(R.layout.fragment_dashboard, container, false);

        mAuth=FirebaseAuth.getInstance();
        FirebaseUser mUser=mAuth.getCurrentUser();
        String uid=mUser.getUid();

        mIncomeDatabase= FirebaseDatabase.getInstance().getReference().child("IncomeDatabase").child(uid);
        mExpenseDatabase=FirebaseDatabase.getInstance().getReference().child("ExpenseDatabase").child(uid);

        mIncomeDatabase.keepSynced(true);// This is used to cache nodes that are really necessary for the app to work offline
        mExpenseDatabase.keepSynced(true);//simply put enabling offline capabilities...

        //Connecting floating button widget to the dashboard layout
        fab_main_btn=myview.findViewById(R.id.fb_main_plus_btn);
        fab_income_btn=myview.findViewById(R.id.income_ft_btn);
        fab_expense_btn=myview.findViewById(R.id.expense_ft_btn);

        //Connecting floating text.
        fab_income_txt=myview.findViewById(R.id.income_ft_text);
        fab_expense_txt=myview.findViewById(R.id.expense_ft_text);

        //Total Income and Expense...

        totalIncomeResult=myview.findViewById(R.id.income_set_result);
        totalExpenseResult=myview.findViewById(R.id.expense_set_result);
        totalBalanceResult=myview.findViewById(R.id.balance_set_result);

        //Recycler View...
        mRecyclerIncome=myview.findViewById(R.id.recycler_income);
        mRecyclerExpense=myview.findViewById(R.id.recycler_expense);


        //Animation to connect floating button..
        FadeOpen= AnimationUtils.loadAnimation(getActivity(),R.anim.fade_open);
        FadeClose=AnimationUtils.loadAnimation(getActivity(),R.anim.fade_close);
        Rob=AnimationUtils.loadAnimation(getActivity(),R.anim.rotate_backward);
        Rof=AnimationUtils.loadAnimation(getActivity(),R.anim.rotate_forward);


        //Implementing bar chart method
        barChart=myview.findViewById(R.id.bar_chart);
        ValueEventListener event2=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getBarEntries(snapshot);
                barDataSet=new BarDataSet(barEntries,"Expenses");// creating a new bar data and  passing the bar data set.
                barData=new BarData(barDataSet);
                barChart.setData(barData);
                barChart.getDescription().setText("Expenses Per Day");
                XAxis xval=barChart.getXAxis();
                xval.setDrawLabels(true);
                xval.setValueFormatter(new IndexAxisValueFormatter(date));
                barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);  // adding color to the bar data set.
                barDataSet.setValueTextColor(Color.BLACK); // setting text colour
                barDataSet.setValueTextSize(16f); // setting text size
                barDataSet.notifyDataSetChanged();
                barChart.notifyDataSetChanged();
                barChart.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        mExpenseDatabase.addListenerForSingleValueEvent(event2);


        //floating action button setting features
        fab_main_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addData();
                if (isOpen){
                    fab_main_btn.startAnimation(Rof);
                    fab_income_btn.startAnimation(FadeClose);
                    fab_expense_btn.startAnimation(FadeClose);
                    fab_income_btn.setClickable(false);
                    fab_expense_btn.setClickable(false);

                    fab_income_txt.startAnimation(FadeClose);
                    fab_expense_txt.startAnimation(FadeClose);
                    fab_income_txt.setClickable(false);
                    fab_expense_txt.setClickable(false);
                    isOpen=false;
                }else {
                    fab_main_btn.startAnimation(Rob);
                    fab_income_btn.startAnimation(FadeOpen);
                    fab_expense_btn.startAnimation(FadeOpen);
                    fab_income_btn.setClickable(true);
                    fab_expense_btn.setClickable(true);

                    fab_income_txt.startAnimation(FadeOpen);
                    fab_expense_txt.startAnimation(FadeOpen);
                    fab_income_txt.setClickable(true);
                    fab_expense_txt.setClickable(true);
                    isOpen=true;
                }
            }
        });

        //Calculate total income...
        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                 totalsumincome=0;

                for (DataSnapshot mysnap:snapshot.getChildren()){
                    Data data=mysnap.getValue(Data.class);

                    totalsumincome+=data.getAmount();

                    String stResult=String.valueOf(totalsumincome);

                    totalIncomeResult.setText("GH₵"+ " " +stResult+".00");//show total income on dashboard dashboard

                }

                balance=totalsumincome-totalsumexpense;//new added line begins here
                if(balance>0.05*totalsumincome && balance<=0.1*totalsumincome){ //condition to alert user when total balance is less than 5%
                    androidx.appcompat.app.AlertDialog.Builder builder=new androidx.appcompat.app.AlertDialog.Builder(getActivity());
                    builder.setTitle("Less than 10% balance remaining!");
                    builder.setMessage("You may need to control your expenses.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //finishAffinity();
                            //startActivity(new Intent(getActivity(),DashboardFragment.class));
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
                else if(balance>0.0*totalsumincome && balance<=0.05*totalsumincome)//condition to alert user when total balance is less than 5%
                {androidx.appcompat.app.AlertDialog.Builder builder=new androidx.appcompat.app.AlertDialog.Builder(getActivity());
                    builder.setTitle("Less than 5% balance remaining!");
                    builder.setMessage("You may need to control your expenses.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //finishAffinity();
                            //startActivity(new Intent(getActivity(),DashboardFragment.class));
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
                String strBalance=String.valueOf(balance);
                totalBalanceResult.setText("GH₵" + " " +strBalance+".00");//sets total balance

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Calculate total expense
        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                 totalsumexpense=0;

                for (DataSnapshot mysnap:snapshot.getChildren()){
                    Data data=mysnap.getValue(Data.class);

                    totalsumexpense+=data.getAmount();

                    String stResult=String.valueOf(totalsumexpense);

                    totalExpenseResult.setText("GH₵"+ " " +stResult+".00");//show total expense on the dashboard

                }

                balance=totalsumincome-totalsumexpense;//check again
                String strBalance=String.valueOf(balance);
                totalBalanceResult.setText("GH₵"+ " " +strBalance+".00");//sets total balance

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //Recycler

        LinearLayoutManager layoutManagerIncome= new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);

        layoutManagerIncome.setStackFromEnd(true);
        layoutManagerIncome.setReverseLayout(true);
        mRecyclerIncome.setHasFixedSize(true);
        mRecyclerIncome.setLayoutManager(layoutManagerIncome);


        LinearLayoutManager layoutManagerExpense= new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);

        layoutManagerExpense.setStackFromEnd(true);
        layoutManagerExpense.setReverseLayout(true);
        mRecyclerExpense.setHasFixedSize(true);
        mRecyclerExpense.setLayoutManager(layoutManagerExpense);


        return myview;
    }

    //Floating button animation
    private void ftAnimation(){
        if (isOpen){
            fab_main_btn.startAnimation(Rob);
            fab_main_btn.startAnimation(Rof);
            fab_income_btn.startAnimation(FadeClose);
            fab_expense_btn.startAnimation(FadeClose);
            fab_income_btn.setClickable(false);
            fab_expense_btn.setClickable(false);

            fab_income_txt.startAnimation(FadeClose);
            fab_expense_txt.startAnimation(FadeClose);
            fab_income_txt.setClickable(false);
            fab_expense_txt.setClickable(false);
            isOpen=false;

        }else {
            fab_income_btn.startAnimation(FadeOpen);
            fab_expense_btn.startAnimation(FadeOpen);
            fab_income_btn.setClickable(true);
            fab_expense_btn.setClickable(true);

            fab_income_txt.startAnimation(FadeOpen);
            fab_expense_txt.startAnimation(FadeOpen);
            fab_income_txt.setClickable(true);
            fab_expense_txt.setClickable(true);
            isOpen=true;

        }
    }

    //Method for addData
    private void addData(){
        fab_income_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incomeDataInsert();
            }
        });

        fab_expense_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expenseDataInsert();
            }
        });
    }

    //Income database (check after new implementations )
    public void incomeDataInsert(){
        AlertDialog.Builder mydialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View myview = inflater.inflate(R.layout.custom_layout_for_insert_data, null);
        mydialog.setView(myview);
        final AlertDialog dialog = mydialog.create();

        dialog.setCancelable(false);
        final EditText edtAmount=myview.findViewById(R.id.amount_edt);
        final EditText edtType=myview.findViewById(R.id.type_edt);
        final EditText edtNote=myview.findViewById(R.id.note_edt);

        Button btnSave=myview.findViewById(R.id.btnSave);
        Button btnCancel=myview.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type=edtType.getText().toString().trim();
                String amount=edtAmount.getText().toString().trim();
                String note=edtNote.getText().toString().trim();

                if (TextUtils.isEmpty(type)){
                    edtType.setError("Required Field..");
                    return;
                }

                if (TextUtils.isEmpty(amount)){
                    edtAmount.setError("Required Field..");
                    return;
                }

                int ouramountint=Integer.parseInt(amount);

                if (TextUtils.isEmpty(note)){
                    edtNote.setError("Required Field..");
                    return;
                }

                //inserting  more stuff to the database
                String id=mIncomeDatabase.push().getKey();//This creates a random id within the database
                String mDate = DateFormat.getDateInstance().format(new Date());//this generates a date

                Data data=new Data(ouramountint,type,note,id,mDate);
                mIncomeDatabase.child(id).setValue(data);//child(id) refers to the generated i.d

                Toast.makeText(getActivity(),"Data ADDED", Toast.LENGTH_SHORT).show();//displays the data added text to user when new data is saved

                ftAnimation();
                dialog.dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ftAnimation();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //expense database (check after new implementations )
    public void expenseDataInsert(){
        AlertDialog.Builder mydialog=new AlertDialog.Builder(getActivity());
        LayoutInflater inflater=LayoutInflater.from(getActivity());
        View myview=inflater.inflate(R.layout.custom_layout_for_insert_data,null);
        mydialog.setView(myview);

        final AlertDialog dialog = mydialog.create();

        dialog.setCancelable(false);

        final EditText amount=myview.findViewById(R.id.amount_edt);
        final EditText type=myview.findViewById(R.id.type_edt);
        final EditText note=myview.findViewById(R.id.note_edt);

        Button btnSave=myview.findViewById(R.id.btnSave);
        Button btnCancel=myview.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tmAmount=amount.getText().toString().trim();
                String tmtype=type.getText().toString().trim();
                String tmnote=note.getText().toString().trim();

                if (TextUtils.isEmpty(tmAmount)){
                    amount.setError("Required Field...");
                    return;
                }

                int intamount=Integer.parseInt(tmAmount);

                if (TextUtils.isEmpty(tmtype)){
                    type.setError("Required Field...");
                    return;
                }
                if (TextUtils.isEmpty(tmnote)){
                    note.setError("Required Field...");
                    return;
                }

                //Expense database

                String id=mExpenseDatabase.push().getKey();
                String mDate=DateFormat.getDateInstance().format(new Date());

                Data data=new Data(intamount,tmtype,tmnote,id,mDate);
                mExpenseDatabase.child(id).setValue(data);
                Toast.makeText(getActivity(),"Data added",Toast.LENGTH_SHORT).show();

                ftAnimation();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ftAnimation();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //Firebase recycler options to listen for data
    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Data> options=
                new FirebaseRecyclerOptions.Builder<Data>()
                        .setQuery(mIncomeDatabase,Data.class)
                        .setLifecycleOwner(this)
                        .build();
        FirebaseRecyclerAdapter<Data, IncomeViewholder> incomeAdapter = new FirebaseRecyclerAdapter<Data, IncomeViewholder>(options) {
            @NonNull
            @Override
            public IncomeViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new IncomeViewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_income,parent,false));
            }
            @Override
            protected void onBindViewHolder(@NonNull IncomeViewholder holder, int position, @NonNull Data model) {

                holder.setmIncomeAmount(model.getAmount());
                holder.setmIncomeType(model.getType());
                holder.setmIncomeDate(model.getDate());

            }
        };
        mRecyclerIncome.setAdapter(incomeAdapter);
        incomeAdapter.startListening();


        options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(mExpenseDatabase, Data.class)
                .setLifecycleOwner(this)
                .build();

        FirebaseRecyclerAdapter<Data, ExpenseViewholder> expenseAdapter = new FirebaseRecyclerAdapter<Data,ExpenseViewholder>(options) {
            @NonNull
            @Override
            public ExpenseViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ExpenseViewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_expense,parent,false));
            }
            @Override
            protected void onBindViewHolder(@NonNull ExpenseViewholder holder, int position, @NonNull Data model) {

                holder.setmExpenseAmount(model.getAmount());
                holder.setmExpenseType(model.getType());
                holder.setmExpenseDate(model.getDate());

            }
        };
        mRecyclerExpense.setAdapter(expenseAdapter);
        expenseAdapter.startListening();



    }


    //For dashboard income...
    public static class IncomeViewholder extends RecyclerView.ViewHolder{

        View mIncomeView;



        public IncomeViewholder(@NonNull View itemView) {
            super(itemView);

            mIncomeView=itemView;

        }

        public void setmIncomeType(String type) {

            TextView mtype=mIncomeView.findViewById(R.id.type_income_ds);
                    mtype.setText(type);
        }


        public void setmIncomeAmount(int amount){

            TextView mamount=mIncomeView.findViewById(R.id.amount_income_ds);
            String stramount=String.valueOf(amount);

            mamount.setText("GH₵"+ " " +stramount+".00");

        }


        public void setmIncomeDate(String date){

            TextView mdate=mIncomeView.findViewById(R.id.date_income_ds);

           mdate.setText(date);


        }


        }


    //For dashboard expense...
    public static class ExpenseViewholder extends RecyclerView.ViewHolder{

        View mExpenseView;

        public ExpenseViewholder(@NonNull View itemView) {
            super(itemView);

            mExpenseView=itemView;

        }

        public void setmExpenseType(String type) {

            TextView mtype=mExpenseView.findViewById(R.id.type_expense_ds);
            mtype.setText(type);
        }


        public void setmExpenseAmount(int amount){

            TextView mamount=mExpenseView.findViewById(R.id.amount_expense_ds);
            String stramount=String.valueOf(amount);

            mamount.setText("GH₵"+ " " +stramount+".00");

        }


        public void setmExpenseDate(String date) {

            TextView mdate = mExpenseView.findViewById(R.id.date_expense_ds);

            mdate.setText(date);

        }



    }

    //Getting bar graph entries
    private void getBarEntries(DataSnapshot snap)
    {

        Log.d("ExpenseData","Reading Data");

        barEntries=new ArrayList();

        float a=1f;
        int a1=1;
        if(snap.exists()) {
            for (DataSnapshot ds : snap.getChildren()) {
                Data data = ds.getValue(Data.class);
                //String date = data.getDate();
                date[a1]=data.getDate().substring(0,7);

                float amm = data.getAmount();
                //String name=ds.child(data.getId()).child("type").getValue(String.class);

                //Data data=ds.getValue(Data.class);
                //String name=data.getType();
                barEntries.add(new BarEntry(a,amm));

                a=a+1;
                a1=a1+1;
            }
        }
    }



}