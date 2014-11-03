//
//  AddYodleeCardVC.h
//  ConcurMobile
//
//  Created by yiwen on 11/4/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "FormViewControllerBase.h"
#import "YodleeCardProvider.h"

@interface AddYodleeCardVC : FormViewControllerBase <UIAlertViewDelegate>
{
    YodleeCardProvider          *provider;
    UIButton                    *btnAddAccount;
    BOOL                        doReload;
    
    BOOL						loadingForm;
	BOOL                        savingForm;

}

@property (strong, nonatomic) UIButton              *btnAddAccount;
@property (strong, nonatomic) YodleeCardProvider    *provider;
@property BOOL loadingForm, savingForm;

- (void)setSeedData:(NSDictionary*)pBag;

@end
