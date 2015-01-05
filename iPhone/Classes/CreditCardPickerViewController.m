//
//  CreditCardPickerViewController.m
//  ConcurMobile
//
//  Created by Richard Puckett on 11/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CreditCardManager.h"
#import "CreditCardPickerViewController.h"

@interface CreditCardPickerViewController ()

@end

@implementation CreditCardPickerViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    if (self) {
        self.title = @"Payment Card";
    }
    
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

#pragma mark - UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView
                             dequeueReusableCellWithIdentifier:@"Cell"];
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"Cell"];
        //cell.selectionStyle = UITableViewCellSelectionStyleNone;
    }
    
    NSInteger row = [indexPath row];
    
    cell.textLabel.text = [[CreditCardManager sharedInstance] cardAtIndex:row];
    
    return cell;
}

/*
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        [tableView beginUpdates];
        
        [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
        
        [tableView endUpdates];
    }
}
 */

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return CreditCardManager.sharedInstance.cards.count;
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    NSInteger row = [indexPath row];
    CreditCardManager.sharedInstance.selectedCardIndex = row;
    
    [self.navigationController popViewControllerAnimated:YES];
}

/*
- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 80;
}
 */

@end
