//
//  RequestHeadeVCr.m
//  ConcurMobile
//
//  Created by Laurent Mery on 14/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "RequestHeaderVC.h"
#import "RequestHeaderForm.h"

#import "CTETravelRequest.h"
#import "WaitViewController.h"

#import "FFFormProtocol.h"

@interface RequestHeaderVC () <FFFormProtocol>

@property (strong, nonatomic) UIBarButtonItem *navButtonSave;

@property (weak, nonatomic) IBOutlet UIView *viewHeader;
@property (weak, nonatomic) IBOutlet UILabel *labelName;

@property (weak, nonatomic) IBOutlet UITableView *tableViewForm;
@property (nonatomic, strong) RequestHeaderForm *headerForm;

@end

@implementation RequestHeaderVC


+(NSString*)viewName{
	
	return @"Requests-Header";
}

- (void)viewDidLoad {
	
	[self updateNavigationBar];	
	[self initElementsSetHidden:YES];
	
    [super viewDidLoad];
	
    [self applyConcurStyle];
    [self applyLocalize];
	
	[self updateDatas];
}

-(void) viewWillAppear:(BOOL)animated{
    
    [super viewWillAppear:animated];
    
    [[CTEDataTypesManager sharedManager] setDateOutputTemplate:@"eeeedMMMMyyyy"];
    
    if (_headerForm && _headerForm.isPendingExternalEdited == YES) {

        [_headerForm refreshCurrentEditedCell];
    }
}

#pragma mark - update view

-(void)updateNavigationBar{
	
	UIBarButtonItem *backButton = [[UIBarButtonItem alloc] initWithTitle:@"" style:UIBarButtonItemStylePlain target:nil action:nil];
	[self.navigationItem setBackBarButtonItem:backButton];
	[self.navigationItem setHidesBackButton:YES];
	UIBarButtonItem *navButton = [[UIBarButtonItem alloc] initWithTitle:[@"Close" localize] style:UIBarButtonItemStylePlain target:self action:@selector(navButtonBackTapped:)];
	[self.navigationItem setLeftBarButtonItem:navButton];
}

-(void)displaySaveButton{
    
    UIBarButtonItem *buttonSave = [[UIBarButtonItem alloc] initWithTitle:[@"Save" localize]
                                                                   style:UIBarButtonItemStylePlain
                                                                  target:self
                                                                  action:@selector(navButtonSaveTapped:)];
    [self.navigationItem setRightBarButtonItem:buttonSave animated:NO];
}


-(void)applyConcurStyle{
    
    [_viewHeader setBackgroundColor:[UIColor backgroundTopHeader]];
    [_labelName setTextColor:[UIColor whiteConcur]];
}


-(void)initElementsSetHidden:(BOOL)hidden{
	
	[_viewHeader setHidden:hidden];
	[_tableViewForm setHidden:hidden];
}


-(void)applyLocalize{
    
    self.navigationItem.title = [@"RequestHeader" localize];
}


#pragma mark - Navigation

-(void)goBack{
    
    [self.navigationController popViewControllerAnimated:YES];
}


-(void)navButtonBackTapped:(id)sender{
	

    if ([self.headerForm isDirtyAllForms]){
     
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:nil
                              message:[@"RPT_SAVE_CONFIRM_MSG" localize] //Your changes may be lost if you proceed.  Would you like to save these changes?
                              delegate:self
                              cancelButtonTitle:[@"Yes" localize]
                              otherButtonTitles:[@"No" localize], nil];
     
        [alert show];
     
     }
     else {
     
         [self goBack];
     }
}

-(void)navButtonSaveTapped:(id)sender{
 
    //TODO: save action
}
 
 
-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex{
 
    if (buttonIndex == 0) { // Yes - save changes
        
        [self navButtonSaveTapped:nil];
    }
    else if (buttonIndex == 1){ // No - exit without changes
        
        [self.headerForm resetAllForms];
        [self goBack];
    }
}



#pragma mark - Datas


-(void)updateDatas{
    
    [WaitViewController showWithText:@"" animated:YES];
    [CCFlurryLogs flurryLogSpinnerStartTimefrom:[self viewName] action:@"load form&fields"];
    
    _headerForm = [[RequestHeaderForm alloc] initWithTableView:_tableViewForm andDelegate:self];
    
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        
        [_headerForm initFormWithDatas:_request];
        
        dispatch_sync(dispatch_get_main_queue(), ^{ //(main thread)
            
            [_labelName setText:[_request.Name stringValue]];
            [self initElementsSetHidden:NO];
            [_headerForm refreshAll];
            
            if ([_request hasPermittedAction:@"save"]){
                
                [self displaySaveButton];
            }
            
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
            [CCFlurryLogs flurryLogSpinnerStopTimefrom:[self viewName] action:@"load form&fields" parameters:nil];
        });
    });
}


//#protocol FFFormController
-(void)pushEditViewController:(UIViewController *)editViewController{
    
    [self.view endEditing:YES];
    [self.navigationController pushViewController:editViewController animated:NO];
}

//#protocol FFFormController
-(void)viewEndEditing{
    
    [self.view endEditing:YES];
}

	

#pragma mark - memory management


- (void)dealloc{
	
	_request = nil;
	
	[CCFlurryLogs flurryLogEventReturnFromView:[self viewName]
									to:self.callerViewName
							parameters:nil];
}


@end
