//
//  CreditCardTableViewController.m
//  ConcurMobile
//
//  Created by Sally Yan on 8/27/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "CreditCardListTableViewController.h"
#import "CTECreditCard.h"

typedef void(^CreditCardSelectionBlock)(CTECreditCard *selectedCreditCard);

@interface CreditCardListTableViewController ()

@property (nonatomic, readonly, strong) NSArray *creditCards;
@property (nonatomic, readonly, assign) CTECreditCard *selectedCreditCard;

// callback block for when a credit card is selected
@property (nonatomic, readonly, copy) CreditCardSelectionBlock creditCardSelectionBlock;

@end

@implementation CreditCardListTableViewController

- (id)initWithCreditCards:(NSArray *)creditCards selectedCard:(CTECreditCard *)selectedCreditCard completion:(void (^)(CTECreditCard *selectedCreditCard))completion
{
    self = [super initWithNibName:@"CreditCardListTableViewController" bundle:nil];
    if (self) {
        _creditCards = creditCards;
        _selectedCreditCard = selectedCreditCard;
        _creditCardSelectionBlock = completion;
        
        [self.tableView registerClass:[UITableViewCell class] forCellReuseIdentifier:@"creditCardCell"];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    self.title = @"Credit Cards";
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.creditCards count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"creditCardCell" forIndexPath:indexPath];
    CTECreditCard *tmp = [self.creditCards objectAtIndex:indexPath.row];
    [cell.textLabel setText:tmp.name];
    
    if ([tmp isEqual:self.selectedCreditCard]) {
        cell.accessoryType = UITableViewCellAccessoryCheckmark;
    } else {
        cell.accessoryType = UITableViewCellAccessoryNone;
    }
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (self.creditCardSelectionBlock) {
        self.creditCardSelectionBlock(self.creditCards[indexPath.row]);
    }
    
    [self.navigationController popViewControllerAnimated:YES];
}

@end
