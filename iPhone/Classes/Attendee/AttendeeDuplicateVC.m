//
//  AttendeeDuplicateVC.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 3/5/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "AttendeeDuplicateVC.h"
#import "SummaryCellMLines.h"

@implementation AttendeeDuplicateVC

@synthesize attendee, duplicates;
@synthesize editorDelegate = _editorDelegate;
@synthesize btnSelect, selectedAttendee, tableList;

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
    [self makeSelectButton:FALSE];
    self.title = [@"Duplicate Attendees" localize];
    
    // MOB-10793 This view controller uses the toolbar.
    [self.navigationController setToolbarHidden:NO];
}


#pragma mark -
#pragma mark Select Button

-(void)makeSelectButton:(BOOL)active 
{
    NSString *addText = [Localizer getLocalizedText:@"Add Selected Attendee"];
    
    CGSize textSize = [addText sizeWithFont:[UIFont boldSystemFontOfSize:12]];
    CGFloat w = 150.0;
    CGFloat h = 30.0;
    
    if((textSize.width + 20) < w)
        w = textSize.width + 20;
    
    if (!active)
    {
        if ([ExSystem is7Plus])
        {
            self.btnSelect = [[UIBarButtonItem alloc] initWithTitle:addText style:UIBarButtonItemStylePlain target:self action:nil];
            [self.btnSelect setEnabled:NO];
        }
        else
            self.btnSelect = [ExSystem makeColoredButton:@"BLUE_INACTIVE" W:w H:h Text:addText SelectorString:nil MobileVC:self];
    }
    else
    {
        if ([ExSystem is7Plus])
        {
            self.btnSelect = [[UIBarButtonItem alloc] initWithTitle:addText style:UIBarButtonItemStylePlain target:self action:@selector(buttonSelectPressed:)];
        }
        else
            self.btnSelect = [ExSystem makeColoredButton:@"BLUE" W:w H:h Text:addText SelectorString:@"buttonSelectPressed:" MobileVC:self];
    }
    
	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	NSArray *toolbarItems = @[flexibleSpace, self.btnSelect];
	[self setToolbarItems:toolbarItems animated:NO];
}

-(IBAction)buttonSelectPressed:(id)sender
{
    [self.navigationController popViewControllerAnimated:NO];
    [self.editorDelegate editedAttendee:selectedAttendee createdByEditor:selectedAttendee == self.attendee];
}

#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 2;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	return (section == 0 ? 1 : [duplicates count]);
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section 
{
    return section == 0 ? [@"New Attendee" localize]:[@"Duplicates Found" localize];
}

- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	return 56;
}

-(SummaryCellMLines*)makeCell:(UITableView*)tblView name:(NSString*)name company:(NSString*)company amount:(NSString*)amount attendeeType:(NSString*)attendeeType
{
	SummaryCellMLines *cell = (SummaryCellMLines *)[tblView dequeueReusableCellWithIdentifier: @"SummaryCell2Lines"];
	
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"SummaryCell2Lines" owner:self options:nil];
		for (id oneObject in nib)
		{
			if ([oneObject isKindOfClass:[SummaryCellMLines class]])
			{
				cell = (SummaryCellMLines *)oneObject;
				break;
			}
		}
	}
	
    NSString* line1 = attendeeType;
    if ([company length])
        line1 = [NSString stringWithFormat:@"%@ - %@", company, attendeeType]; 
    cell.singleSelect = YES;
    [cell resetCellContent:name withAmount:amount withLine1:line1 withLine2:nil withImage1:nil withImage2:nil withImage3:nil];
	[cell layoutWithSelect:YES];
	return cell;
}


- (UITableViewCell *)tableView:(UITableView *)tblView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];
    NSUInteger section = [indexPath section];
    
	UITableViewCell *cell = nil;
    
    // Configure the cell...
	AttendeeData *atn = section == 0? attendee : duplicates[row];
	NSString *name = [atn getFullName];
	NSString *company = [atn getNonNullableValueForFieldId:@"Company"];
	NSString *atnTypeName = [atn getNonNullableValueForFieldId:@"AtnTypeName"];
    
    NSString* amt = @"";
    
	cell = [self makeCell:tblView name:name company:company amount:amt attendeeType:atnTypeName];
    
    [cell setAccessoryType:UITableViewCellAccessoryNone];
    
    return cell;
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];
    NSUInteger section = [indexPath section];
    self.selectedAttendee = section == 0? attendee : duplicates[row];
    [self makeSelectButton:YES];
}

@end
