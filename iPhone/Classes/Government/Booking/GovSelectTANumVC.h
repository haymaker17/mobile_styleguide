//
//  GovSelectTANumVC.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/15/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MobileViewController.h"
#import "FieldEditDelegate.h"
#import "GovTANumber.h"

@interface GovSelectTANumVC : MobileViewController<UITableViewDelegate, UITableViewDataSource, FieldEditDelegate>
{
    UITableView				*tableList;

    NSMutableArray          *taNumbers;
    BOOL                    selectedNewTANum;
    GovTANumber             *selectedTANumber;
    
    NSMutableArray          *rows;
    
    // Initial Parameter
    NSString                *actionAfterCompletion;
    NSArray                 *taFields;  // Two fields, auth and perDiem
    id<FieldEditDelegate>	__weak _delegate;
}

@property (nonatomic, strong) IBOutlet UITableView      *tableList;
@property (nonatomic, strong) NSMutableArray            *taNumbers;
@property BOOL selectedNewTANum;
@property (nonatomic, strong) GovTANumber               *selectedTANumber;
@property (nonatomic, strong) NSMutableArray            *rows;
@property (nonatomic, strong) NSString                  *actionAfterCompletion;
@property (nonatomic, strong) NSArray                   *taFields;
@property (weak, nonatomic) id<FieldEditDelegate>       delegate;

-(void) setSeedData:(NSArray*) taFields;
-(IBAction)actionClose:(id)sender;

+(void) showSelectTANum:(UIViewController*)pvc withCompletion:(NSString*)booking withFields:(NSArray*) taFields withDelegate:(id<FieldEditDelegate>) del asRoot:(BOOL)isRoot;

@end
