//
//  ExUnitTestsVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/24/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ExUnitTestsVC.h"
#import "ExUnitTestCell.h"
#import "ExSystem.h"
#import "ExUnitTestDescriber.h"
#import "FormatUtils.h"

@implementation ExUnitTestsVC
@synthesize aRows, dictValues, unitTests, lblResults, tbBottom;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}


- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.unitTests = [[ExUnitTests alloc] init];
    
    self.aRows = unitTests.aTestNames;
    self.dictValues = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];
    for(NSString *s in aRows)
    {
        ExUnitTestDescriber *d = [[ExUnitTestDescriber alloc] init];
        d.isFail = NO;
        d.failString = @"";
        dictValues[s] = d;
    }

    //self.dictValues = unitTests.dictTests;

}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}


#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [aRows count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];
    NSString *key = aRows[row];
    ExUnitTestDescriber *describer = dictValues[key];
	
    static NSString *MyIdentifier = @"ExUnitTestCell";
	
    ExUnitTestCell *cell = (ExUnitTestCell *)[tableView dequeueReusableCellWithIdentifier: MyIdentifier];
    if (cell == nil)  
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ExUnitTestCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[ExUnitTestCell class]])
                cell = (ExUnitTestCell *)oneObject;
    }
    
    if(describer.isFail)
    {
        cell.iv.image = [UIImage imageNamed:@"flightStatsDotRed"];
        cell.lbl.text = [NSString stringWithFormat:@"%@: %@", aRows[row], describer.failString];
        cell.lbl.numberOfLines = 20;
        float h = 44.0;
        float w = 320;
        if(cell.lbl != nil)
            w = cell.lbl.frame.size.width;
        NSString *deathText = [NSString stringWithFormat:@"%@: %@", aRows[row], describer.failString];
        h = [FormatUtils getTextFieldHeight:w Text:deathText FontSize:17.0f];
        if(cell.lbl != nil)
            cell.lbl.frame = CGRectMake(cell.lbl.frame.origin.x, cell.lbl.frame.origin.y, cell.lbl.frame.size.width, h);
    }
    else 
    {
        cell.iv.image = [UIImage imageNamed:@"flightStatsDotGreen"];
        cell.lbl.text = aRows[row];
        if(cell.lbl != nil)
            cell.lbl.frame = CGRectMake(cell.lbl.frame.origin.x, cell.lbl.frame.origin.y, cell.lbl.frame.size.width, 44.0);
    }
    
    
    return cell;

}


#pragma mark -
#pragma mark Table View Delegate Methods
//- (NSString *)tableView:(UITableView *)tableView 
//titleForHeaderInSection:(NSInteger)section
//{
//    NSString *key = [keys objectAtIndex:section];
//    return key;
//}


- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{

    NSUInteger row = [indexPath row];
    NSString *key = aRows[row];
    ExUnitTestDescriber *describer = dictValues[key];
    if(describer.isFail)
    {
//        ExUnitTestCell *cell = (ExUnitTestCell *)[tableView cellForRowAtIndexPath:indexPath];
//        cell.lbl.numberOfLines = 50;
        float h = 44.0;
        //float w = cell.lbl.frame.size.width;
        NSString *deathText = [NSString stringWithFormat:@"%@: %@", aRows[row], describer.failString];
        h = [FormatUtils getTextFieldHeight:258 Text:deathText FontSize:17.0f];
        //cell.lbl.frame = CGRectMake(cell.lbl.frame.origin.x, cell.lbl.frame.origin.y, cell.lbl.frame.size.width, h);
        return h;
    }
    else
		return 44;
}


- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath 
{
	
    NSUInteger row = [indexPath row];

    NSString *key = aRows[row];
    
    NSMutableArray *a = [unitTests runTest:key];
    if(a != nil)
    {
        ExUnitTestCell *cell = (ExUnitTestCell *)[tableView cellForRowAtIndexPath:indexPath];
        if([self hasFails:a])
        {
            cell.iv.image = [UIImage imageNamed:@"flightStatsDotRed"];
            [tbBottom setTintColor:[UIColor redColor]];
            ExUnitTestDescriber *describer = dictValues[key];
            describer.failString = [self getFails:a];
            describer.isFail = YES;
            dictValues[key] = describer;
            lblResults.text = @"Fail!"; //[self getFails:a];
            [tableView reloadData];
        }
        else
        {
            cell.iv.image = [UIImage imageNamed:@"flightStatsDotGreen"];
            [tbBottom setTintColor:[UIColor greenColor]];
            lblResults.text = [NSString stringWithFormat:@"%d tests run (%@)", [a count], [NSDate date]];
        }   
    }
    
}

-(BOOL) hasFails:(NSMutableArray *)a
{
    for(ExUnitTestDescriber *result in a)
    {
//        if([[result substringToIndex:4] isEqualToString:@"FAIL"])
//            return YES;
        if(result.isFail)
            return YES;
        
    }
    
    return NO;
}

-(NSString *) getFails:(NSMutableArray *)a
{
    NSMutableString *s = [[NSMutableString alloc] initWithString:@""];
    int failCount = 0;
    
    for(ExUnitTestDescriber *result in a)
    {        
        if(result.isFail)
        {
            failCount++;
            if([s length] > 0)
                [s appendString:@"\n"];
            
            [s appendString:result.failString];
        }
            
    }
    
    //NSString *rVal = [NSString stringWithFormat:@"%d Failures: %@", failCount, s];
    NSString *rVal = [NSString stringWithFormat:@"%@", s];
    return rVal;
}


#pragma mark -
#pragma mark Button Methods
-(IBAction) buttonClose:(id)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

@end
