ownCloud-Account-Importer
=========================

Account Importer (Android Library Project)



How to use it?
--------------

1) you'll need to extend **IAccountImport**

That means that you have to implement the following method:
    
    public void accountAccessGranted(OwnCloudAccount account);


As you can see in the following example, it's really easy to get the account data

    @Override
    public void accountAccessGranted(OwnCloudAccount account) {
        mUsernameView.setText(account.getUsername());
        mPasswordView.setText(account.getPassword());
        mOc_root_path_View.setText(account.getUrl());
    }


And then you can call the dialog with the following code:

    public static void show(FragmentActivity activity, IAccountImport accountImport)


Here a small example:

    view.findViewById(R.id.btn_importAccount).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ImportAccountsDialogFragment.show(getActivity(), LoginDialogFragment.this);
        }
    });

    //If no other accounts (from other apps) are available.. hide the button
    if(AccountImporter.findAccounts(getActivity()).size() <= 0) {
        view.findViewById(R.id.btn_importAccount).setVisibility(View.GONE);
    }