//
//  AbstractViewController.m
//  ConcurMobile
//
//  Created by Richard Puckett on 10/7/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AbstractViewController.h"
#import "FirstViewController.h"
#import "JPTUtils.h"
#import "RouteExpenseManager.h"
#import "RouteManager.h"
#import "SelectReportViewController.h"
#import "UIDevice+Additions.h"

@interface AbstractViewController ()

@end

@implementation AbstractViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    if (self)
    {
        UIBarButtonItem *saveButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemSave target:self action:@selector(onSaveTapped:)];
        
        [self.navigationItem setRightBarButtonItem:saveButton];
    }
    
    return self;
}

- (BOOL)canSaveFavorite {
    return NO;
}

- (void)gotoIndex {
    [self gotoIndexToTab:-1];
}

- (void)gotoIndexToTab:(NSInteger)tabIndex {
    for (UIViewController *controller in self.navigationController.viewControllers) {
        if ([controller isKindOfClass:[FirstViewController class]]) {
            FirstViewController *fvc = (FirstViewController *) controller;
            
            if (tabIndex != -1) {
                fvc.typeControl.selectedSegmentIndex = tabIndex;
            }
            
            [self.navigationController popToViewController:fvc
                                                  animated:YES];
            break;
        }
    }
}

- (void)gotoReport {
    if([UIDevice isPad]) {
        [self gotoReportForTablet];
	}
	else {
        [self gotoReportForPhone];
	}
}

- (void)gotoReportForTablet {
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                 self.chosenReport, @"REPORT",
                                 ROLE_EXPENSE_TRAVELER, @"ROLE",
                                 self.chosenReport.rptKey, @"ID_KEY",
                                 self.chosenReport.rptKey, @"RECORD_KEY",
                                 @"YES", @"SHORT_CIRCUIT", nil];
    
    // this forces the next screen to at least refresh the summary.  This pBag system is dumb. - Ernest
    pBag[@"COMING_FROM"] = @"REPORT";
    
    [self.parentViewController dismissViewControllerAnimated:YES completion:nil];
    
    ReportDetailViewController_iPad *newDetailViewController = [[ReportDetailViewController_iPad alloc] initWithNibName:@"ReportDetailViewController_iPad" bundle:nil];
    
    newDetailViewController.role = ROLE_EXPENSE_TRAVELER;
    
    newDetailViewController.isReport = YES;
    
    UINavigationController *homeNavigationController = (UINavigationController*)self.presentingViewController;
    
    [homeNavigationController pushViewController:newDetailViewController animated:YES];
    
    [newDetailViewController loadReport:pBag];
}

- (void)gotoReportForPhone {
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                 self.chosenReport, @"REPORT",
                                 ROLE_EXPENSE_TRAVELER, @"ROLE",
                                 self.chosenReport.rptKey, @"ID_KEY",
                                 self.chosenReport.rptKey, @"RECORD_KEY",
                                 @"YES", @"SHORT_CIRCUIT", nil];
    
    ReportDetailViewController *vc = [[ReportDetailViewController alloc]
                                      initWithNibName:@"ReportHeaderView"
                                      bundle:nil];
    
    [vc setSeedData:pBag];
    
    NSMutableArray *controllers = [[NSMutableArray alloc] init];
    
    for (UIViewController *controller in self.navigationController.viewControllers) {
        [controllers addObject:controller];
        
        if ([controller isKindOfClass:[FirstViewController class]]) {
            break;
        }
    }
    
    [controllers addObject:vc];
    
    [self.navigationController setViewControllers:controllers animated:YES];
}

- (BOOL)isFormComplete {
    // Override if using form.
    
    return YES;
}

- (void)onSaveTapped:(id)sender {
    if ([self isFormComplete]) {
        [self.view endEditing:YES];
        
        UIActionSheet *expenseActionSheet = [[UIActionSheet alloc]
                                             initWithTitle:nil
                                             delegate:self
                                             cancelButtonTitle:[Localizer getLocalizedText:@"Cancel"]
                                             destructiveButtonTitle:nil
                                             otherButtonTitles:
                                             [Localizer getLocalizedText:@"save_for_later"],
                                             [Localizer getLocalizedText:@"add_to_report"],
                                             nil];
        
        [expenseActionSheet showInView:self.view];
    } else {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:nil
                                                        message:[Localizer getLocalizedText:@"Please enter values for required fields, in red, before saving."]
                                                       delegate:nil
                                              cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                              otherButtonTitles:nil];
        
        [alert show];
    }
}

- (void)saveAsExpense {
    [[RouteExpenseManager sharedInstance] saveExpense:self.routeExpense];
    
    if ([self canSaveFavorite]) {
        if (self.routeExpense.isFavorite) {
            [[RouteExpenseManager sharedInstance] saveFavoriteRouteExpense:self.routeExpense];
        }
    }
    
    [self gotoIndexToTab:2];
}

- (void)selectReport {
    SelectReportViewController *vc = [[SelectReportViewController alloc] init];
    vc.delegate = self;
    
    self.navigationItem.backBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Back"]
                                                                             style:UIBarButtonItemStylePlain
                                                                            target:nil
                                                                            action:nil];
    
    [self.navigationController pushViewController:vc animated:YES];
}

#pragma mark - Notification dialogs

- (void)notifyUserOfRouteAdded {
    NSString *message = [NSString stringWithFormat:@"%@ %@",
                         [Localizer getLocalizedText:@"route_added_to_report_confirmation"],
                         self.chosenReport.reportName];
    
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:nil
                                                    message:message
                                                   delegate:self
                                          cancelButtonTitle:[Localizer getLocalizedText:@"back_to_list"]
                                          otherButtonTitles:[Localizer getLocalizedText:@"view_report"], nil];
    
    alert.tag = 100;
    
    [alert show];
}

- (void)notifyUserOfError:(NSError *)error {
    NSString *errorMessage = [Localizer getLocalizedText:@"Unable to attach expense. Please check that this report policy allows Japan Public Transit."];
    
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:nil
                                                    message:errorMessage
                                                   delegate:nil
                                          cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                          otherButtonTitles:nil];
    alert.tag = 200;
    
    [alert show];
}

#pragma mark - UIActionSheetDelegate

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
    switch (buttonIndex) {
        case 0:
            [self saveAsExpense];
            break;
        case 1:
            [self selectReport];
            break;
    }
}

#pragma mark - UIAlertView delegate

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    switch (alertView.tag) {
        case 100:
            if (buttonIndex == 0) {
                [self gotoIndex];
            } else {
                [self gotoReport];
            }
            break;
    }
}

#pragma mark - ReportActionDelegate

- (void)didChooseReport:(ReportData *)report {
    self.chosenReport = report;
    
    if ([self canSaveFavorite]) {
        if (self.routeExpense.isFavorite) {
            [[RouteExpenseManager sharedInstance] saveFavoriteRouteExpense:self.routeExpense];
        }
    }
    
    UIAlertView *progressAlert = [[UIAlertView alloc] initWithTitle:nil
                                                            message:[Localizer getLocalizedText:@"Saving Expense"]
                                                           delegate:nil
                                                  cancelButtonTitle:nil
                                                  otherButtonTitles:nil];
    
    UIActivityIndicatorView *progress = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    
    [progressAlert addSubview:progress];
    
    [progress startAnimating];
    
    [progressAlert show];
    
    progress.frame = CGRectMake(progressAlert.frame.size.width / 2 - progress.frame.size.width,
                                progressAlert.frame.size.height - progress.frame.size.height * 2,
                                progress.frame.size.width,
                                progress.frame.size.height);
    
    
    [JPTUtils addRouteExpense:self.routeExpense toReport:report
                      success:^(NSString *result) {
                          [progressAlert dismissWithClickedButtonIndex:0 animated:YES];
                          //[self gotoIndex];
                          [self gotoReport];
                      } failure:^(NSError *error) {
                          [progressAlert dismissWithClickedButtonIndex:0 animated:YES];
                          [self notifyUserOfError:error];
                      }];
}

@end
